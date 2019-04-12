package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A minimalistic representation of a round with matches and teams.
 *
 * @author Bas Piepers
 */
@DataObject
public class Round implements JsonDomainObject {
    private final int round;
    private final Instant scheduledStartTime;
    private final Instant scheduledEndTime;
    private final List<Match> matches;

    private Round(int round, Instant scheduledStartTime, Instant scheduledEndTime, List<Match> matches) {
        this.round = round;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.matches = matches;
    }

    public Round(JsonObject jsonObject) {
        this.round = jsonObject.getInteger("round");
        this.scheduledStartTime = jsonObject.getInstant("scheduledStartTime");
        this.scheduledEndTime = jsonObject.getInstant("scheduledEndTime");
        this.matches = jsonObject
                .getJsonArray("matches")
                .stream()
                .map(o -> new Match((JsonObject) o))
                .collect(Collectors.toList());
    }

    public int getRound() {
        return round;
    }

    public Instant getScheduledStartTime() {
        return scheduledStartTime;
    }

    public Instant getScheduledEndTime() {
        return scheduledEndTime;
    }

    public List<Match> getMatches() {
        return matches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Round period = (Round) o;

        if (round != period.round) return false;
        if (!scheduledStartTime.equals(period.scheduledStartTime)) return false;
        if (!scheduledEndTime.equals(period.scheduledEndTime)) return false;
        return matches.equals(period.matches);

    }

    @Override
    public int hashCode() {
        int result = round;
        result = 31 * result + scheduledStartTime.hashCode();
        result = 31 * result + scheduledEndTime.hashCode();
        result = 31 * result + matches.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Period{" +
                "round=" + round +
                ", scheduledStartTime=" + scheduledStartTime +
                ", scheduledEndTime=" + scheduledEndTime +
                ", matches=" + matches +
                '}';
    }
}
