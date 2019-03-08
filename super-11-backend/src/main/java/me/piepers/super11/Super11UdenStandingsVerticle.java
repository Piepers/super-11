package me.piepers.super11;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.reactivex.core.AbstractVerticle;
import me.piepers.super11.domain.Competition;

import me.piepers.super11.reactivex.domain.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A prototype that holds a cache with the standings of our competition. Updated once after a game round and once
 * every hour during a game round.
 *
 * @author Bas Piepers
 */
public class Super11UdenStandingsVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Super11UdenStandingsVerticle.class);

    private static final Integer TWO_HOURS = 1000 * 3600;
    // The cashed "competition" which is the standings of our league
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

    private void handleTimer(Long timerId) {
        LOGGER.debug("Timer triggered with id {}");
        // Populate/update the competition object with the contents from the api.
        // TODO: implement
//        competitionService.rxFetchLatestCompetitionStandings().subscribe();
    }
}
