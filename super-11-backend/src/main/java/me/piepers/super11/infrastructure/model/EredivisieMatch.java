package me.piepers.super11.infrastructure.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * The match part of the round data structure that comes back from the Eredivisie.nl api call. Parts that we don't need
 * are not going to be mapped and are also not added to this class.
 *
 * @author Bas Piepers
 */
@DataObject
public class EredivisieMatch {
    private final String gameId;
    private String venue;
    private String dateLong;
    private String score;
    private String status;
    private String period;
    private String winner;
    private String matchdetail;
    private String statusToString;
    private String statusToStringBottom;
    private String round;
    private Integer roundint;
    private String date;
    private String roundtype;
    private String team1ID;
    private String team1Side;
    private String team1Goals;
    private String team1Name;
    private String team2ID;
    private String team2Side;
    private String team2Goals;
    private String team2Name;
    private Integer team1GoalsConceded;
    private Integer team2GoalsConceded;

    public EredivisieMatch(JsonObject jsonObject) {
        this.gameId = jsonObject.getString("gameId");
        this.venue = Objects.nonNull(jsonObject.getString("venue")) ? jsonObject.getString("venue") : null;
        this.dateLong = Objects.nonNull(jsonObject.getString("dateLong")) ? jsonObject.getString("dateLong") : null;
        this.score = Objects.nonNull(jsonObject.getString("score")) ? jsonObject.getString("score") : null;
        this.status = Objects.nonNull(jsonObject.getString("status")) ? jsonObject.getString("status") : null;
        this.period = Objects.nonNull(jsonObject.getString("period")) ? jsonObject.getString("period") : null;
        this.winner = Objects.nonNull(jsonObject.getString("winner")) ? jsonObject.getString("winner") : null;
        this.matchdetail = Objects.nonNull(jsonObject.getString("matchdetail")) ? jsonObject.getString("matchdetail") : null;
        this.statusToString = Objects.nonNull(jsonObject.getString("statusToString")) ? jsonObject.getString("statusToString") : null;
        this.statusToStringBottom = Objects.nonNull(jsonObject.getString("statusToStringBottom")) ? jsonObject.getString("statusToStringBottom") : null;
        this.round = Objects.nonNull(jsonObject.getString("round")) ? jsonObject.getString("round") : null;
        this.roundint = Objects.nonNull(jsonObject.getInteger("roundint")) ? jsonObject.getInteger("roundint") : null;
        this.date = Objects.nonNull(jsonObject.getString("date")) ? jsonObject.getString("date") : null;
        this.roundtype = Objects.nonNull(jsonObject.getString("roundtype")) ? jsonObject.getString("roundtype") : null;
        this.team1ID = Objects.nonNull(jsonObject.getString("team1ID")) ? jsonObject.getString("team1ID") : null;
        this.team1Side = Objects.nonNull(jsonObject.getString("team1Side")) ? jsonObject.getString("team1Side") : null;
        this.team1Goals = Objects.nonNull(jsonObject.getString("team1Goals")) ? jsonObject.getString("team1Goals") : null;
        this.team1Name = Objects.nonNull(jsonObject.getString("team1Name")) ? jsonObject.getString("team1Name") : null;
        this.team2ID = Objects.nonNull(jsonObject.getString("team2ID")) ? jsonObject.getString("team2ID") : null;
        this.team2Side = Objects.nonNull(jsonObject.getString("team2Side")) ? jsonObject.getString("team2Side") : null;
        this.team2Goals = Objects.nonNull(jsonObject.getString("team2Goals")) ? jsonObject.getString("team2Goals") : null;
        this.team2Name = Objects.nonNull(jsonObject.getString("team2Name")) ? jsonObject.getString("team2Name") : null;
        this.team1GoalsConceded = Objects.nonNull(jsonObject.getInteger("team1GoalsConceded")) ? jsonObject.getInteger("team1GoalsConceded") : null;
        this.team2GoalsConceded = Objects.nonNull(jsonObject.getInteger("team2GoalsConceded")) ? jsonObject.getInteger("team2GoalsConceded") : null;
    }

    public String getGameId() {
        return gameId;
    }

    public String getVenue() {
        return venue;
    }

    public String getDateLong() {
        return dateLong;
    }

    public String getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public String getPeriod() {
        return period;
    }

    public String getWinner() {
        return winner;
    }

    public String getMatchdetail() {
        return matchdetail;
    }

    public String getStatusToString() {
        return statusToString;
    }

    public String getStatusToStringBottom() {
        return statusToStringBottom;
    }

    public String getRound() {
        return round;
    }

    public Integer getRoundint() {
        return roundint;
    }

    public String getDate() {
        return date;
    }

    public String getRoundtype() {
        return roundtype;
    }

    public String getTeam1ID() {
        return team1ID;
    }

    public String getTeam1Side() {
        return team1Side;
    }

    public String getTeam1Goals() {
        return team1Goals;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public String getTeam2ID() {
        return team2ID;
    }

    public String getTeam2Side() {
        return team2Side;
    }

    public String getTeam2Goals() {
        return team2Goals;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public Integer getTeam1GoalsConceded() {
        return team1GoalsConceded;
    }

    public Integer getTeam2GoalsConceded() {
        return team2GoalsConceded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EredivisieMatch match = (EredivisieMatch) o;

        return gameId.equals(match.gameId);

    }

    @Override
    public int hashCode() {
        return gameId.hashCode();
    }

    @Override
    public String toString() {
        return "Match{" +
                "gameId='" + gameId + '\'' +
                ", venue='" + venue + '\'' +
                ", dateLong='" + dateLong + '\'' +
                ", score='" + score + '\'' +
                ", status='" + status + '\'' +
                ", period='" + period + '\'' +
                ", winner='" + winner + '\'' +
                ", matchdetail='" + matchdetail + '\'' +
                ", statusToString='" + statusToString + '\'' +
                ", statusToStringBottom='" + statusToStringBottom + '\'' +
                ", round='" + round + '\'' +
                ", roundint=" + roundint +
                ", date='" + date + '\'' +
                ", roundtype='" + roundtype + '\'' +
                ", team1ID='" + team1ID + '\'' +
                ", team1Side='" + team1Side + '\'' +
                ", team1Goals='" + team1Goals + '\'' +
                ", team1Name='" + team1Name + '\'' +
                ", team2ID='" + team2ID + '\'' +
                ", team2Side='" + team2Side + '\'' +
                ", team2Goals='" + team2Goals + '\'' +
                ", team2Name='" + team2Name + '\'' +
                ", team1GoalsConceded=" + team1GoalsConceded +
                ", team2GoalsConceded=" + team2GoalsConceded +
                '}';
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
