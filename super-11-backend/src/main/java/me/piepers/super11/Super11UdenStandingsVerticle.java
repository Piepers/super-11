package me.piepers.super11;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import me.piepers.super11.domain.Competition;
import me.piepers.super11.reactivex.domain.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A prototype that holds a cache with the standings of our competition. Takes care of caching lookup data and schedules
 * to determine in what frequency the standings of our league needs to be polled.
 *
 * @author Bas Piepers
 */
public class Super11UdenStandingsVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Super11UdenStandingsVerticle.class);

    private static final Integer TWO_HOURS = 1000 * 3600;

    private static final Integer TWENTY_FOUR_HOURS = 1000 * 3600 * 24;

    // The cached "competition" which is the standings of our league. Is updated by a timer so reads may be "dirty".
    private Competition competition;

    // TODO: put this in configuration - also needs the X-Client-Game in the header and the X-Game-Group
    private static final String PROGRAM_URL = "https://gameapi.chromasports.com/fixtures/next-game-periods";


    private io.vertx.reactivex.core.Vertx rxVertx;
    private CompetitionService competitionService;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.rxVertx = new io.vertx.reactivex.core.Vertx(vertx);
        this.competitionService = CompetitionService.createProxy(rxVertx);

        rxVertx.setPeriodic(TWO_HOURS, this::handleTimer);

    }

    @Override
    public void start(Future<Void> future) {
        // Cache a competition to begin with
        competitionService
                .rxFetchLatestCompetitionStandings()
                .doOnSuccess(competition -> this.competition = competition)
                .subscribe(competition -> future.complete(),
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

    // TODO: would be nice if we could use a cron-tab style kind of timing so that it occurs once every night or something.
    private void handleDailyLookups(Long timerId) {
        LOGGER.debug("Handling daily lookups...");

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
