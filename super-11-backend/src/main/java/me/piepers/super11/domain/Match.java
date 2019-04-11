package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

/**
 * Minimalistic representation of a match that is mainly used to determine if a match is played at any given time.
 *
 * @author Bas Piepers
 */
@DataObject
public class Match implements JsonDomainObject {
    private final Team home;
    private final Team away;
    private final Instant scheduledStartTime;
    private final Instant scheduledEndTime;

    private Match(Team home, Team away, Instant scheduledStartTime, Instant scheduledEndTime) {
        this.home = home;
        this.away = away;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
    }

    public Match(JsonObject jsonObject) {
        this.home = new Team(jsonObject.getJsonObject("team"));
        this.away = new Team(jsonObject.getJsonObject("away"));
        this.scheduledStartTime = jsonObject.getInstant("scheduledStartTime");
        this.scheduledEndTime = jsonObject.getInstant("scheduledEndTime");
    }

    public static Match of(Team home, Team away, Instant scheduledStartTime, Instant scheduledEndTime) {
        return new Match(home, away, scheduledStartTime, scheduledEndTime);
    }

    public Team getHome() {
        return home;
    }

    public Team getAway() {
        return away;
    }

    public Instant getScheduledStartTime() {
        return scheduledStartTime;
    }

    public Instant getScheduledEndTime() {
        return scheduledEndTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        if (!home.equals(match.home)) return false;
        if (!away.equals(match.away)) return false;
        if (!scheduledStartTime.equals(match.scheduledStartTime)) return false;
        return scheduledEndTime.equals(match.scheduledEndTime);

    }

    @Override
    public int hashCode() {
        int result = home.hashCode();
        result = 31 * result + away.hashCode();
        result = 31 * result + scheduledStartTime.hashCode();
        result = 31 * result + scheduledEndTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Match{" +
                "home=" + home +
                ", away=" + away +
                ", scheduledStartTime=" + scheduledStartTime +
                ", scheduledEndTime=" + scheduledEndTime +
                '}';
    }
}
