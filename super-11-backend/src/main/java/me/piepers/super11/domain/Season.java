package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.infrastructure.model.EredivisieSeason;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Season with the rounds, matches and teams. Is cached and stored to disk
 *
 * @author Bas Piepers
 */
@DataObject
public class Season implements JsonDomainObject {
    private final String name;
    private final String country;
    // The instant when this object was last updated with the latest content
    private final Instant lastUpdated;
    private final List<Round> rounds;

    public Season(JsonObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.country = jsonObject.getString("country");
        this.lastUpdated = jsonObject.getInstant("lastUpdated", Instant.now());
        this.rounds = jsonObject.getJsonArray("rounds", new JsonArray()).stream().map(o -> new Round((JsonObject) o)).collect(Collectors.toList());
    }

    private Season(String name, String country, Instant lastUpdated, List<Round> rounds) {
        this.name = name;
        this.country = country;
        this.lastUpdated = lastUpdated;
        this.rounds = rounds;
    }

    /**
     * Maps a season of the Eredivisie.nl site to a season of this instance.
     *
     * @param eredivisieSeason, the season as obtained from an external API.
     * @return an instance of this season.
     */
    public static final Season from(EredivisieSeason eredivisieSeason) {
        String name = "Eredivisie";
        String country = "NL";
        Instant updated = Instant.now();
        List<Round> rounds = eredivisieSeason
                .getRounds()
                .stream()
                .map(eredivisieRound -> Round.from(eredivisieRound))
                .collect(Collectors.toList());
        return new Season(name, country, updated, rounds);
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Season season = (Season) o;

        if (!name.equals(season.name)) return false;
        return country.equals(season.country);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + country.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Season{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", rounds=" + rounds +
                '}';
    }
}
