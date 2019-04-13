package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.infrastructure.model.EredivisieRound;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("UTC"));

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

    /**
     * Maps a round from the Eredivisie to an instance of this class. The start date of the round is derived from the
     * matches of this round because the round itself doesn't have a proper start date stored with it (only a human
     * readable version that can not be directly parsed to a start and end date).
     *
     * @param eredivisieRound, the eredivisie round that was obtained from an external API.
     * @return an instance of this round.
     */
    public static Round from(EredivisieRound eredivisieRound) {
        String firstMatchDateTime =
                eredivisieRound
                        .getMatches()
                        .stream()
                        .map(em -> em.getDate())
                        .sorted()
                        .collect(Collectors.toList())
                        .get(0);

        Instant scheduledStartTime = convertEredivisieDateTimeToInstant(firstMatchDateTime);
        // End date is also a date time in UTC (but without timezone information).
        Instant scheduledEndTime = convertEredivisieDateTimeToInstant(eredivisieRound.getEnddate());
        int round = Integer.parseInt(eredivisieRound.getRound());
        // Parse the matches one-by-one and grab what's interesting to have.
        List<Match> matches = eredivisieRound
                .getMatches()
                .stream()
                .map(eredivisieMatch -> Match
                        .of(Team.of(eredivisieMatch.getTeam1ID(), eredivisieMatch.getTeam1Name()),
                                Team.of(eredivisieMatch.getTeam2ID(), eredivisieMatch.getTeam2Name()),
                                convertEredivisieDateTimeToInstant(eredivisieMatch.getDate())))
                .collect(Collectors.toList());
        return new Round(round, scheduledStartTime, scheduledEndTime, matches);
    }

    /**
     * The date time fields in the eredivisie API have a format that doesn't contain timezone information but are in UTC.
     * In order for us to store it as an instant, we need to parse and convert them.
     *
     * @param dateTime, the datetime that is expected to have a format of yyyy-MM-ddTHH:mm:ss.
     * @return the same date/time but converted to an instant.
     */
    private static Instant convertEredivisieDateTimeToInstant(String dateTime) {
        ZonedDateTime zdt = ZonedDateTime.parse(dateTime, dtf);
        return zdt.toInstant();
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
