package me.piepers.super11.infrastructure.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * We get a response from the eredivisie.nl API that just contains a bunch of items in an Json array. This class can map
 * the content to a structure that we can use in our application.
 * <p>
 * This payload is stored on disk and read once the application is loaded. The last checked field indicates when the
 * schedule of the season has been checked for the last time as we use it to periodically update it using an external
 * api.
 *
 * @author Bas Piepers
 */
@DataObject
public class Season {
    private final Instant lastChecked;
    private final List<Round> rounds;

    public Season(JsonObject jsonObject) {
        this.lastChecked = jsonObject.getInstant("lastChecked");
        this.rounds = jsonObject.getJsonArray("rounds").stream().map(o -> new Round((JsonObject) o)).collect(Collectors.toList());

    }

    public Instant getLastChecked() {
        return lastChecked;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
