package me.piepers.super11.application.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.domain.Draft;

/**
 * The standings as they are retrieved from the backend api contain information that we do not need in the front-end.
 * This Dto represent the standings so that the UI only gets what it needs.
 *
 * @author Bas Piepers
 */
@DataObject
public class StandingsDto {
    private final Integer rank;
    private final String draftName;
    private final Integer points;
    private final Long totalPoints;

    public StandingsDto(JsonObject jsonObject) {
        this.rank = jsonObject.getInteger("rank");
        this.draftName = jsonObject.getString("draftName");
        this.points = jsonObject.getInteger("points");
        this.totalPoints = jsonObject.getLong("totalPoints");
    }

    private StandingsDto(Integer rank, String draftName, Integer points, Long totalPoints) {
        this.rank = rank;
        this.draftName = draftName;
        this.points = points;
        this.totalPoints = totalPoints;
    }

    public static StandingsDto from(Draft draft) {
        return new StandingsDto(draft.getRank(), draft.getDraftName(), draft.getPoints(), draft.getTotalPoints());
    }

    public Integer getRank() {
        return rank;
    }

    public String getDraftName() {
        return draftName;
    }

    public Integer getPoints() {
        return points;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    @Override
    public String toString() {
        return "StandingsDto{" +
                "rank=" + rank +
                ", draftName='" + draftName + '\'' +
                ", points=" + points +
                ", totalPoints=" + totalPoints +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StandingsDto that = (StandingsDto) o;

        if (!rank.equals(that.rank)) return false;
        if (!draftName.equals(that.draftName)) return false;
        if (!points.equals(that.points)) return false;
        return totalPoints.equals(that.totalPoints);
    }

    @Override
    public int hashCode() {
        int result = rank.hashCode();
        result = 31 * result + draftName.hashCode();
        result = 31 * result + points.hashCode();
        result = 31 * result + totalPoints.hashCode();
        return result;
    }
}
