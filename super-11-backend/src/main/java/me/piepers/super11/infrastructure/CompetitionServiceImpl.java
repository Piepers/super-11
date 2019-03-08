package me.piepers.super11.infrastructure;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceException;
import me.piepers.super11.domain.AuthRequestBody;
import me.piepers.super11.domain.Competition;
import me.piepers.super11.domain.CompetitionService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * {@inheritDoc}
 */
public class CompetitionServiceImpl implements CompetitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompetitionServiceImpl.class);
    private Vertx rxVertx;
    private WebClient webClient;

    // The default path the access key, obtained from the OAuth flow, is stored.
    private static final String DEFAULT_ACCESS_KEY_PATH = "/etc/super-11/";
    private static final String DEFAULT_ACCESS_KEY_FILE_NAME = "access_key";

    private String accessKeyPath;
    private String accessKeyFileName;
    private String cachedAccessKey;

    // A temporary storage of cookies for the oauth flow.
    private List<String> cookies = new ArrayList<>();

    // FIXME: don't store this on this instance.
    private JsonObject configuration;

    public CompetitionServiceImpl(io.vertx.core.Vertx vertx, JsonObject configuration) {
        this.rxVertx = new Vertx(vertx);
        this.configuration = configuration;

        JsonObject profcoachAuth = configuration.getJsonObject("profcoach-auth", new JsonObject());
        String path = Optional.ofNullable(profcoachAuth.getString("access_key_location_path")).orElse(DEFAULT_ACCESS_KEY_PATH);
        String fileName = Optional.ofNullable(profcoachAuth.getString("access_key_location_file_name")).orElse(DEFAULT_ACCESS_KEY_FILE_NAME);
        this.accessKeyPath = path;
        this.accessKeyFileName = fileName;

        LOGGER.debug("Trying to obtain the access key from: {}{}", path, fileName);

        String directory = path.lastIndexOf('/') == path.length() - 1 ? path.substring(0, path.length() - 1) : path;
        // Read the contents of the file and store it in the cached access-key.
        rxVertx
                .fileSystem()
                .rxExists(directory)
                .flatMapCompletable(exists -> {
                    if (exists) {
                        return Completable.complete();
                    } else {
                        return rxVertx.fileSystem().rxMkdirs(directory);
                    }
                })
                .andThen(rxVertx.fileSystem().rxReadFile(directory + File.separator + fileName))
                .doOnSuccess(buffer -> LOGGER.trace("Read {} from file.", buffer.toString()))
                .subscribe(buffer -> this.cachedAccessKey = buffer.toString(),
                        throwable -> LOGGER.error("Unable to obtain access key from local file. No key will be cached!"));


        // TODO: probably not necessary to create a pool because of the low amount of requests.
        this.webClient = WebClient
                .create(rxVertx, new WebClientOptions()
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .setMaxPoolSize(2));
    }

    @Override
    public void fetchLatestCompetitionStandings(Handler<AsyncResult<Competition>> result) {
        Single<String> accessKeyResult = null;
        if (Objects.isNull(this.cachedAccessKey)) {
            // First time we fetch the access key so start an access key flow
            accessKeyResult = this.requestAccessKey();
        } else {
            LOGGER.debug("We have an acceskey so use that.");
            accessKeyResult = Single.just(this.cachedAccessKey);
        }

        JsonObject profcoachConfig = configuration.getJsonObject("profcoach-auth", new JsonObject());
        String gameApiHost = profcoachConfig.getString("game_api_host", "");
        String gameApiUrl = profcoachConfig.getString("game_api_standings_url", "");
        Integer port = profcoachConfig.getInteger("game_api_port");
        String xClientGame = profcoachConfig.getString("x_client_game");
        String xGameGroup = profcoachConfig.getString("x_game_group");

        accessKeyResult
                .flatMap(accessKey -> webClient
                        .get(port, gameApiHost, gameApiUrl)
                        .ssl(true)
                        .bearerTokenAuthentication(accessKey)
                        .putHeader("Content-Type", "application/json")
                        .putHeader("X-Client-Game", xClientGame)
                        .putHeader("X-Game-Group", xGameGroup)
                        .rxSend())
                .subscribe(httpResponse -> {
                            if (httpResponse.statusCode() == 200) {
                                Competition competition = new Competition(httpResponse.bodyAsJsonObject());
                                result.handle(Future.succeededFuture(competition));
                            } else {
                                // FIXME: implement retry so that a new access key is obtained in case it was no longer valid.
                                LOGGER.debug("Unable to obtain competition standing. Statuscode was: {}. Get a new access key?", httpResponse.statusCode());
                                result.handle(Future.failedFuture(new ServiceException(httpResponse.statusCode(), httpResponse.statusMessage())));
                            }
                        },
                        throwable -> result.handle(Future.failedFuture(throwable)));


        // Make the request. If we receive an "unauthorized" response: fetch the key and do the response again.

    }

    /**
     * Store the access key in the file that was configured to contain the access key. Create the directory and file in
     * case it doesn't exist yet.
     *
     * @param accessKey, the accesskey to store.
     */
    private void storeAccessKey(String accessKey) {
        rxVertx
                .fileSystem()
                .rxWriteFile(accessKeyPath + accessKeyFileName, Buffer.buffer(accessKey))
                .subscribe(() -> LOGGER.debug("Stored accesskey to {}", accessKeyPath + accessKeyFileName),
                        throwable -> LOGGER.error("Unable to store access key", throwable));
    }

    // Returns an access key that will also be stored to a file so that it can be obtained from that file if the server restarts.
    private Single<String> requestAccessKey() {
        JsonObject authConfig = Optional
                .ofNullable(configuration
                        .getJsonObject("profcoach-auth"))
                .orElseThrow(() -> new ServiceException(500, "Unable to obtain authentication configuration necessary to start the oauth flow"));

        String oAuthStartHost = authConfig.getString("oauth_start_host");
        Integer oAuthStartPort = authConfig.getInteger("oauth_start_port");
        String oAuthStartUrl = authConfig.getString("oauth_start_url");
        LOGGER.debug("Requesting access key starting with host: {}, port: {} and url: {}", oAuthStartHost, oAuthStartPort, oAuthStartUrl);

        AuthRequestBody body = AuthRequestBody.fromConfiguration(authConfig);
        LOGGER.debug("Using this body on initial request: \n{}", body.toJson().encodePrettily());

        return webClient
                .post(oAuthStartPort, oAuthStartHost, oAuthStartUrl)
                .ssl(true)
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(body.toJson())
                .flatMap(response -> this.processInitialResponse(authConfig, response))
                .flatMap(response -> this.processAuthorizeResponse(authConfig, response))
                .flatMap(response -> this.processOAuthAccess(authConfig, response))
                .flatMap(response -> this.processOAuthResponse(response))
                .doOnSuccess(accessKey -> this.storeAccessKey(accessKey))
                .doOnSuccess(accessKey -> this.cachedAccessKey = accessKey)
                .doFinally(() -> this.cookies.clear())
                .doOnError(throwable -> LOGGER.error("Something went wrong while processing the oauth flow", throwable))
                .doOnError(throwable -> throwable.printStackTrace());

    }

    private Single<String> processOAuthResponse(HttpResponse<Buffer> response) {
        if (response.statusCode() == 200) {
            // TODO: exception handling in case the below content is not present.
            JsonObject responseBody = response.bodyAsJsonObject();
            JsonObject data = responseBody.getJsonObject("data");
            JsonObject token = data.getJsonObject("token");
            return Single.just(token.getString("access_token"));

        } else {
            return Single.error(() -> new ServiceException(424, "The last oauth response did not return with a successful status code but with: " + response.statusCode()));
        }
    }

    private Single<HttpResponse<Buffer>> processOAuthAccess(JsonObject authConfig, HttpResponse<Buffer> response) throws ServiceException {
        LOGGER.debug("We should now have an access token in our location header.");
        String accessToken = "";

        Map.Entry<String, String> location = response
                .headers()
                .entries()
                .stream()
                .peek(header -> LOGGER.debug("Header of the authorize response: {} | {}", header.getKey(), header.getValue()))
                .filter(header -> header.getKey().equals("Location"))
                .findFirst()
                .orElseThrow(() -> new ServiceException(424, "No location header was found in the oAuth response which is required to determine where to submit the oAuth request to."));

        // After the access_token parameter, we will find the value of the token we need.
        Optional<String> accessTokenOptional = Arrays
                .stream(location
                        .getValue()
                        .substring(location
                                .getValue()
                                .indexOf('#'))
                        .split("&"))
                .filter(parameter -> parameter
                        .startsWith("access_token"))
                .findFirst();

        if (accessTokenOptional.isPresent()) {
            String[] keyValue = accessTokenOptional.get().split("=");
            accessToken = keyValue.length > 0 ? keyValue[1] : "";
            LOGGER.debug("Using {} to request a bearer token", accessToken);

            // TODO: exception handling in case these keys are missing.
            String xGameGroup = authConfig.getString("x_game_group");
            String gameApiHost = authConfig.getString("game_api_host");
            String gameApiUrl = authConfig.getString("game_api_url");
            Integer gameApiPort = authConfig.getInteger("game_api_port");
            // This token is now used as the authorization header in the client request
            return webClient
                    .get(gameApiPort, gameApiHost, gameApiUrl)
                    .ssl(true)
                    // TODO: is the X-Game-Group a fixed value or can this be obtained from one of the response headers/cookies?
                    .putHeader("X-Game-Group", xGameGroup)
                    .putHeader("Content-Type", "application/json")
                    .putHeader("Authorization", accessToken)
                    .rxSend();
        }
        return Single.error(() -> new ServiceException(424, "There was no access token in the response of the oAuth request."));
    }

    private Single<HttpResponse<Buffer>> processAuthorizeResponse(JsonObject authConfig, HttpResponse<Buffer> response) {
        if (response.statusCode() == 200) {
            if (Objects.nonNull(response.cookies())) {
                this.cookies.addAll(response.cookies());
            }
            String cookieValue = this.collectCookies(this.cookies);
            Document d = Jsoup.parse(response.bodyAsString());
            // There should only one form with this class
            Elements forms = d.select(".pure-form");
            String action = "";
            MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
            if (forms.size() != 1) {
                LOGGER.error("Found more than one form with the pure-form class name.");
                return Single.error(() -> new ServiceException(424, "Found more than one form with the pure-form class name."));
            } else {
                // Get the action from the form
                FormElement form = (FormElement) forms.get(0);
                action = form.attr("action");
                LOGGER.debug("We will submit to: {}", action);
                // With the formData call I can create a MultiMap for the WebClient to send the data in the form to the action attribute
                form.formData().stream().forEach(fe -> {
                    // Set the value of "IsApproved" to true because it will set the consent value to true
                    if (fe.key().equals("IsApproved")) {
                        fe.value("true");
                    }
                    multiMap.add(fe.key(), fe.value());
                });
            }
            String destination = authConfig.getString("destination_host", "");
            Integer port = authConfig.getInteger("destination_port", 443);
            return webClient
                    .post(port, destination, action)
                    .ssl(true)
                    .putHeader("Content-Type", "application/x-www-form-urlencoded")
                    .putHeader("Cookie", cookieValue)
                    .rxSendForm(multiMap);

        } else {
            return Single.error(() -> new ServiceException(424, "The authorize response did not respond with a successful http code."));
        }
    }

    private Single<HttpResponse<Buffer>> processInitialResponse(JsonObject authConfig, HttpResponse<Buffer> response) {
        if (response.statusCode() == 200) {
            this.cookies.addAll(response.cookies());
            String cookieValue = Objects.nonNull(this.cookies) ? this.collectCookies(this.cookies) : "";
            JsonObject loginResponse = response.bodyAsJsonObject();
            LOGGER.debug("Login response: {}", loginResponse.encodePrettily());
            String gotoString = loginResponse.getString("Goto");

            LOGGER.debug("The goto string is: {}", gotoString);

            URL url = null;
            try {
                url = new URL(gotoString);
            } catch (MalformedURLException e) {
                return Single.error(e);
            }

            String path = url.getPath();
            String host = url.getHost();
            Integer port = url.getPort();
            HttpRequest<Buffer> httpRequest = webClient.get(port, host, path);
            Map<String, List<String>> params = this.splitQuery(url);
            // TODO: determine whether we have enough like this: the list can contain more values since we get a list of items back from the params.
            params.entrySet().forEach(entry -> httpRequest.addQueryParam(entry.getKey(), entry.getValue().get(0)));
            httpRequest.ssl(true);
            return httpRequest.putHeader("Cookie", cookieValue)
                    .rxSend();
        } else {
            // TODO: make this something meaningful.
            return Single.error(() -> new ServiceException(424, "The initial request did not return with a successful error code but with: " + response.statusCode()));
        }
    }

    // Collects all cookies, separated by a ';' except for the last cookie
    private String collectCookies(List<String> cookies) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String cookie : cookies) {
            if (cookies.indexOf(cookie) == cookies.size() - 1) {
                stringBuffer.append(cookie);
            } else {
                stringBuffer.append(cookie).append(';');
            }
        }
        return stringBuffer.toString();
    }

    public Map<String, List<String>> splitQuery(URL url) {
        if (Objects.isNull(url.getQuery()) || "".equals(url.getQuery())) {
            return Collections.emptyMap();
        }
        return Arrays.stream(url.getQuery().split("&"))
                .map(this::splitQueryParameter)
                .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue, toList())));
    }

    public AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

}
