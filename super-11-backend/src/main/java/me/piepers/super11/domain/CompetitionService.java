package me.piepers.super11.domain;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.infrastructure.CompetitionServiceImpl;

/**
 * Gets the standings of the competition. Also retrieves a new access token if the current token is expired.
 *
 * @author Bas Piepers
 */
@VertxGen
@ProxyGen
public interface CompetitionService {
    String EVENT_BUS_ADDRESS = "me.piepers.profcoach.domain.CompetitionService";

    static CompetitionService create(Vertx vertx, JsonObject configuration) {
        return new CompetitionServiceImpl(vertx, configuration);
    }

    static CompetitionService createProxy(Vertx vertx) {
        return new CompetitionServiceVertxEBProxy(vertx, EVENT_BUS_ADDRESS);
    }

    /**
     * Fetches the latest competition standings from the Profcoach API so that it can be updated in our cache.
     *
     * @param result, the ayns-result of the update
     */

    void fetchLatestCompetitionStandings(Handler<AsyncResult<Competition>> result);
}
