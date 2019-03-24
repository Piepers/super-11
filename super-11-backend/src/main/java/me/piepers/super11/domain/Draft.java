package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Represents the drafts in the {@link CompetitionData} object which is probably used for multiple purposes in the
 * API. Initially in this application it will represent the standings in the league.
 *
 * @author Bas Piepers
 */
@DataObject
public class Draft {
    private final String id;
    private final String draftName;
    private final Long totalPoints;
    private final Boolean isUser;
    private final Integer rank;
    private final Integer movement;
    private final Integer points;
    private final Integer previousPoints;
    private final Integer previousRank;
    //    private final String managerName;
    private final Boolean isEmpty;
    private final String avatarUrl;
    private final Boolean highLight;

    public Draft(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.draftName = jsonObject.getString("draftName");
        this.totalPoints = jsonObject.getLong("totalPoints");
        this.isUser = jsonObject.getBoolean("isUser");
        this.rank = jsonObject.getInteger("rank");
        this.movement = jsonObject.getInteger("movement");
        this.points = jsonObject.getInteger("points");
        this.previousPoints = jsonObject.getInteger("previousPoints");
        this.previousRank = jsonObject.getInteger("previousRank");
//        this.managerName = jsonObject.getString("managerName");
        this.isEmpty = jsonObject.getBoolean("isEmpty");
        this.avatarUrl = jsonObject.getString("avatarUrl");
        this.highLight = jsonObject.getBoolean("highLight");
    }

    public String getId() {
        return id;
    }

    public String getDraftName() {
        return draftName;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }

    public Boolean getUser() {
        return isUser;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getMovement() {
        return movement;
    }

    public Integer getPoints() {
        return points;
    }

    public Integer getPreviousPoints() {
        return previousPoints;
    }

    public Integer getPreviousRank() {
        return previousRank;
    }

//    public String getManagerName() {
//        return managerName;
//    }

    public Boolean getEmpty() {
        return isEmpty;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Boolean getHighLight() {
        return highLight;
    }

    @Override
    public String toString() {
        return "Draft{" +
                "id='" + id + '\'' +
                ", draftName='" + draftName + '\'' +
                ", totalPoints=" + totalPoints +
                ", isUser=" + isUser +
                ", rank=" + rank +
                ", movement=" + movement +
                ", points=" + points +
                ", previousPoints=" + previousPoints +
                ", previousRank=" + previousRank +
//                ", managerName='" + managerName + '\'' +
                ", isEmpty=" + isEmpty +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", highLight=" + highLight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Draft draft = (Draft) o;

        return id.equals(draft.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
