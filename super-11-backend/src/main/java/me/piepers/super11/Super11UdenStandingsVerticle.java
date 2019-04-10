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
 * A prototype that holds a cache with the standings of our competition. Updated once after a game round and once
 * every hour during a game round.
 *
 * @author Bas Piepers
 */
public class Super11UdenStandingsVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Super11UdenStandingsVerticle.class);

    private static final Integer TWO_HOURS = 1000 * 3600;

    // The cached "competition" which is the standings of our league. Is updated by a timer so reads may be "dirty".
    private Competition competition;

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

    private void handleTimer(Long timerId) {
        LOGGER.debug("Timer triggered with id {}", timerId);

        // Populate/update the competition object with the contents from the api.
        competitionService
                .rxFetchLatestCompetitionStandings()
                .doOnSuccess(competition -> LOGGER.debug("Pusblishing updated competition to the event bus..."))
                .doOnSuccess(competition -> vertx.eventBus().publish("competition.update", competition))
                .subscribe(competition -> this.competition = competition,
                        throwable -> LOGGER.error("Unable to fetch competition data."));
    }
}
