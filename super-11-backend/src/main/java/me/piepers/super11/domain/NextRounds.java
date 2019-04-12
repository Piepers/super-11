package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.infrastructure.model.Season;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the rounds that are left to be played. Contains rounds and matches. Is primarily to determins whether
 * there is currently a match. Is calculated based on the current time and what comes from an API with all the rounds
 * in a season.
 *
 * @author Bas Piepers
 */
@DataObject
public class NextRounds implements JsonDomainObject {
    // If we are at the end of a season, this would contain an empty list.
    private final List<Round> nextRounds;

    public NextRounds(JsonObject jsonObject) {
        this.nextRounds = jsonObject.getJsonArray("nextRounds", new JsonArray()).stream().map(o -> new Round((JsonObject) o)).collect(Collectors.toList());
    }

    public NextRounds(List<Round> nextRounds) {
        this.nextRounds = nextRounds;
    }

    /**
     * Based on the contents of the season, creates an instance of this class which contains the bare minimum there is
     * to know to determine whether a round is currently underway.
     *
     * @param season, the season as obtained from the Eredivisie.nl API
     * @return all the next rounds of the season as of now. If there is currently a round on its way, then this will
     * also be present.
     */
    public static NextRounds from(Season season) {
        Objects.requireNonNull(season);


        return null;
    }

}
