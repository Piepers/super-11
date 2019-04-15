package me.piepers.super11.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.infrastructure.model.EredivisieSeason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Season with the rounds, matches and teams. Is cached and stored to disk
 *
 * @author Bas Piepers
 */
@DataObject
public class Season implements JsonDomainObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(Season.class);
    private final String name;
    private final String country;
    // The instant when this object was last updated with the latest content
    private final Instant lastUpdated;
    private final List<Round> rounds;

    public Season(JsonObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.country = jsonObject.getString("country");
        this.lastUpdated = jsonObject.getInstant("lastUpdated", Instant.now());
        this.rounds = jsonObject.getJsonArray("rounds", new JsonArray())
                .stream()
                .map(o -> new Round((JsonObject) o))
                .collect(Collectors.toList());
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

    @JsonIgnore
    public boolean isMatchActiveNow() {
        return this.isMatchActiveAt(Instant.now());
    }

    @JsonIgnore
    public boolean isMatchActiveAt(Instant at) {
        Optional<Round> round = this.rounds
                .stream()
                .filter(r -> at.isAfter(r.getScheduledStartTime()) && at.isBefore(r.getScheduledEndTime()))// Filter on round that is active
                .findFirst();
        if (round.isPresent()) {
            return round.get()
                    .getMatches()
                    .stream()
                    .filter(match -> at.isAfter(match.getScheduledStartTime()) && at.isBefore(match.getScheduledStartTime().plus(110, ChronoUnit.MINUTES)))
                    .peek(match -> LOGGER.debug("Found matching match of {} - () with a start time of {} at time {}", match.getHome().getName(), match.getAway().getName(), match.getScheduledStartTime(), at))
                    .findFirst()
                    .isPresent();
        } else {
            return false;
        }
    }

    public List<Match> whichMatchesAreActiveNow() {
        return this.whichMatchesAreActiveAt(Instant.now());
    }

    public List<Match> whichMatchesAreActiveAt(Instant at) {
        LOGGER.debug("Which matches are active at {}?", at.toString());
        return this.rounds
                .stream()
                .filter(round -> at.isAfter(round.getScheduledStartTime()) && at.isBefore(round.getScheduledEndTime()))// Filter on round that is active and expect only one.
                .findFirst()
                .map(round -> round
                        .getMatches()
                        .stream()
                        .filter(match -> at.isAfter(match.getScheduledStartTime()) && at.isBefore(match.getScheduledStartTime().plus(110, ChronoUnit.MINUTES)))// Assumption: a match is probably ended after 110 minutes.
                        .peek(match -> LOGGER.debug("Found active match {} - {} because it started at {} and assumed end time of: {}", match.getHome().getName(), match.getAway().getName(), match.getScheduledStartTime().toString(), match.getScheduledStartTime().plus(110, ChronoUnit.MINUTES)))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

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
