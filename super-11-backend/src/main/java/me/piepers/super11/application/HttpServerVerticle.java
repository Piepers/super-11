package me.piepers.super11.application;

import io.reactivex.Single;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.stomp.BridgeOptions;
import io.vertx.ext.stomp.StompServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.stomp.StompServer;
import io.vertx.reactivex.ext.stomp.StompServerHandler;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import me.piepers.super11.application.model.StandingsDto;
import me.piepers.super11.domain.Competition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);
    private static final int DEFAULT_HTTP_PORT = 8080;
    private int port;
    private io.vertx.reactivex.core.Vertx rxVertx;
    public static final String UPDATE_STOMP_DESTINATION = "update-standings";

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.rxVertx = new io.vertx.reactivex.core.Vertx(vertx);
        JsonObject httpServerConfig = context.config().getJsonObject("http_server");
        int port = Objects.nonNull(httpServerConfig) ? httpServerConfig.getInteger("port", DEFAULT_HTTP_PORT) : DEFAULT_HTTP_PORT;
        LOGGER.debug("Working with port number: {}. Configuration contained: {}", Objects.nonNull(httpServerConfig) ? httpServerConfig.getInteger("port", 0) : "Nothing", httpServerConfig.encodePrettily());
        this.port = port;

        // Start a consumer that publishes the competition standings on the stomp address when it is updated by the standingsverticle.
        rxVertx
                .eventBus()
                .<JsonObject>consumer("competition.update", message -> {
                    JsonObject body = message.body();
                    Competition competition = new Competition(body);
                    this.handleCompetitionUpdate(competition);
                });
    }

    @Override
    public void start(Future<Void> future) {
        StompServerOptions stompServerOptions = new StompServerOptions()
                .setPort(-1)
                .setWebsocketBridge(true)
                .setWebsocketPath("/stomp");

        BridgeOptions bridgeOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress(UPDATE_STOMP_DESTINATION));

        StompServer stompServer = StompServer
                .create(vertx, stompServerOptions)
                .handler(StompServerHandler.create(vertx).bridge(bridgeOptions));

        Router router = Router.router(vertx);
        Router subRouter = Router.router(vertx);
        subRouter.route(HttpMethod.GET, "/standings").handler(this::competitionHandler);
        router.mountSubRouter("/api", subRouter);

        this.vertx
                .createHttpServer(new HttpServerOptions().setWebsocketSubProtocols("v10.stomp, v11.stomp, v12.stomp"))
                .websocketHandler(stompServer.webSocketHandler())
                .requestHandler(router)
                .rxListen(this.port)
                .doOnSuccess(result -> LOGGER.debug("Http Server has been started on port {}", this.port))
                .subscribe(result ->
                                future.complete(),
                        throwable -> future.fail(throwable));
    }

    private void handleCompetitionUpdate(Competition competition) {
        LOGGER.debug("Received a new update from the standings verticle. Publishing that to the event bus.");
        // Map it to something we can publish to the UI.
        List<JsonObject> result = competition
                .getData()
                .getDrafts()
                .stream()
                .map(draft -> StandingsDto.from(draft))
                .map(standingsDto -> standingsDto.toJson())
                .collect(Collectors.toList());
        rxVertx
                .eventBus()
                .publish(UPDATE_STOMP_DESTINATION, new JsonObject().put("drafts", new JsonArray(result)));
    }

    private void competitionHandler(RoutingContext routingContext) {
        this.getLatestStandings()
                .subscribe(result -> routingContext
                                .response()
                                .setStatusCode(200)
                                .putHeader("Content-Type", "application/json; charset=UTF-8")
                                // FIXME: make more restrict in production.
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(result.encode()),
                        throwable -> routingContext
                                .response()
                                .setStatusCode(500)
                                .putHeader("Content-Type", "application/json; charset=UTF-8")
                                .end(new JsonObject().put("Error", throwable
                                        .getMessage())
                                        .encode(), StandardCharsets.UTF_8.name()));
    }

    private Single<JsonObject> getLatestStandings() {
        return vertx
                .eventBus()
                .<JsonObject>rxSend("get.competition", new JsonObject())
                .map(result -> new Competition(result.body()))
                .map(competition -> competition.getData().getDrafts().stream().map(draft -> StandingsDto.from(draft)).collect(Collectors.toList()))
                .map(list -> new JsonObject().put("drafts", new JsonArray(list)));
    }
}
