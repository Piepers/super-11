package me.piepers.super11;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.WebClient;
import me.piepers.super11.domain.Competition;
import me.piepers.super11.infrastructure.model.EredivisieSeason;
import me.piepers.super11.reactivex.domain.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.Objects;

/**
 * A prototype that holds a cache with the standings of our competition. Takes care of caching lookup data and schedules
 * to determine in what frequency the standings of our league needs to be polled.
 *
 * @author Bas Piepers
 */
public class Super11UdenStandingsVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Super11UdenStandingsVerticle.class);

    private static final String DEFAULT_STORAGE_PATH = "/var/super-11/";
    private static final String DEFAULT_SEASON_STORAGE_FILE_NAME = "season.json";
    private static final Integer TWO_HOURS = 1000 * 3600;
    private static final Integer TWENTY_FOUR_HOURS = 1000 * 3600 * 24;

    // The cached "competition" which is the standings of our league. Is updated by a timer so reads may be "dirty".
    private Competition competition;
    // The cached season which is retrieved from a file and updated daily.
    private EredivisieSeason season;

    private io.vertx.reactivex.core.Vertx rxVertx;
    private CompetitionService competitionService;

    private String storagePath = DEFAULT_STORAGE_PATH;
    private String seasonFile = DEFAULT_SEASON_STORAGE_FILE_NAME;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.rxVertx = new io.vertx.reactivex.core.Vertx(vertx);
        this.competitionService = CompetitionService.createProxy(rxVertx);
        JsonObject standingsConfig = context.config().getJsonObject("standings");
        LOGGER.debug("Obtained standingsconfig: {}", Objects.nonNull(standingsConfig) ? standingsConfig.encodePrettily() : "no config was present...");
        if (Objects.nonNull(standingsConfig)) {
            String storagePath = standingsConfig.getString("local_storage_path", DEFAULT_STORAGE_PATH);
            String seasonFile = standingsConfig.getString("season_file_name", DEFAULT_SEASON_STORAGE_FILE_NAME);
            this.storagePath = storagePath;
            this.seasonFile = seasonFile;
        }
        this.storagePath = this.storagePath.endsWith(File.separator) ? this.storagePath : this.storagePath + File.separator;
        this.seasonFile = this.seasonFile.startsWith(File.separator) ? this.seasonFile.substring(1, this.seasonFile.length() - 1) : this.seasonFile;

        LOGGER.debug("Using storage path/file: {}{}", this.storagePath, this.seasonFile);

        rxVertx.setPeriodic(TWO_HOURS, this::handleTimer);
        rxVertx.setPeriodic(TWENTY_FOUR_HOURS, this::handleDailyLookups);
    }

    @Override
    public void start(Future<Void> future) {
        // Cache a competition to begin with and cache/update the season contents.
        competitionService
                .rxFetchLatestCompetitionStandings()
                .doOnSuccess(competition -> LOGGER.debug("Competition read successfully. Contents:\n{}", competition.toJson().encodePrettily()))
                .doOnSuccess(competition -> this.competition = competition)
                .ignoreElement()
                .andThen(rxVertx.fileSystem().rxExists(storagePath))
                .flatMapCompletable(exists -> {
                    if (exists) {
                        return Completable.complete();
                    } else {
                        return rxVertx.fileSystem().rxMkdirs(storagePath);
                    }
                })
                .andThen(rxVertx.fileSystem().rxExists(storagePath + seasonFile))
                .flatMap(exists -> {
                    if (exists) {
                        return this.readSeasonFromFile();
                    } else {
                        return this.fetchSeasonFromApi()
                                .doOnSuccess(season -> this.writeSeasonToFile(season));
                    }
                })
                .doOnSuccess(season -> this.season = season)
                .subscribe(season -> future.complete(),
                        throwable -> future.fail(throwable));

        vertx
                .eventBus()
                .<JsonObject>consumer("get.competition", message -> {
                    if (Objects.isNull(this.competition)) {
                        message.fail(500, "No competition was cached yet.");
                    } else {
                        message.reply(this.competition.toJson());
                    }
                });
    }

    // TODO: handle error situations better.
    private void writeSeasonToFile(EredivisieSeason season) {
        LOGGER.debug("Writing season to file {}, overwriting existing file.", storagePath + seasonFile);
        vertx
                .fileSystem()
                .rxWriteFile(storagePath + seasonFile, Buffer.buffer(season.toJson().encode()))
                .subscribe(() -> LOGGER.debug("Season file was written successfully."),
                        throwable -> LOGGER.error("Error occurred when writing season file", throwable));

    }

    private Single<EredivisieSeason> readSeasonFromFile() {
        return
                vertx
                        .fileSystem()
                        .rxReadFile(storagePath + seasonFile)
                        .doOnSuccess(buffer -> LOGGER.debug("Successfully read contents from file."))
                        .flatMap(buffer -> Single
                                .just(new EredivisieSeason(buffer
                                        .toJsonObject())));
    }

    private Single<EredivisieSeason> fetchSeasonFromApi() {
        return WebClient
                .create(vertx, new WebClientOptions())
                // TODO: make the urls etc configurable.
                .get(443, "eredivisie.nl", "/nl-nl/DesktopModules/DotControl/DCEredivisieLive/API/Match/GetAllRounds")
                .addQueryParam("moduleId", "416")
                .addQueryParam("tabId", "95")
                .addQueryParam("showNext", "false")
                .putHeader("Accept", "application/json")
                .ssl(true)
                .rxSend()
                .doOnSuccess(response -> LOGGER.debug("Received response with code: {} and body:\n{}", response.statusCode(), response.bodyAsString()))
                .flatMap(response -> {
                    if (response.statusCode() == 200) {
                        JsonArray jsonArray = new JsonArray(response.bodyAsString());
                        JsonObject jsonObject = new JsonObject()
                                .put("lastChecked", Instant.now())
                                .put("rounds", jsonArray);
                        EredivisieSeason season = new EredivisieSeason(jsonObject);
                        return Single.just(season);
                    } else {
                        return Single.error(new Exception("Something went wrong while requesting new season information from the api. Site code: " + response.statusCode()));
                    }
                });

    }

    // TODO: would be nice if we could use a cron-tab style kind of timing so that it occurs once every night or something.
    private void handleDailyLookups(Long timerId) {
        LOGGER.debug("Handling daily lookups with timer id: {}", timerId);
        this.fetchSeasonFromApi()
                .doOnSuccess(season -> this.writeSeasonToFile(season))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe(season -> this.season = season, throwable -> LOGGER.error("Unable to update the season contents", throwable));

    }

    private void handleTimer(Long timerId) {
        LOGGER.debug("Timer triggered with id {}", timerId);

        // Populate/update the competition object with the contents from the api.
        competitionService
                .rxFetchLatestCompetitionStandings()
                .doOnSuccess(competition -> LOGGER.debug("Publishing updated competition to the event bus..."))
                .doOnSuccess(competition -> vertx.eventBus().publish("competition.update", competition))
                .subscribe(competition -> this.competition = competition,
                        throwable -> LOGGER.error("Unable to fetch competition data."));
    }
}
