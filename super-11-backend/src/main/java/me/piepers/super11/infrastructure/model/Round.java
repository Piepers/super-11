package me.piepers.super11.infrastructure.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * What comes back from the Eredivisie.nl api is a collection of rounds with matches in them. This is what we will
 * flatten in our application to something useful. This class only contains the fields that we think may be potentially
 * useful.
 *
 * @author Bas Piepers
 */
@DataObject
public class Round {
    private final String round;
    private final String title;
    private final String fromto;
    private String active;
    private final String enddate;
    private final List<Match> matches;

    public Round(JsonObject jsonObject) {
        this.round = jsonObject.getString("round");
        this.title = jsonObject.getString("title");
        this.fromto = jsonObject.getString("fromto");
        this.active = Objects.nonNull("active") ? jsonObject.getString("active") : null;
        this.enddate = jsonObject.getString("enddate");
        this.matches = jsonObject.getJsonArray("matches").stream().map(o -> new Match((JsonObject) o)).collect(Collectors.toUnmodifiableList());
    }

    public String getRound() {
        return round;
    }

    public String getTitle() {
        return title;
    }

    public String getFromto() {
        return fromto;
    }

    public String getActive() {
        return active;
    }

    public String getEnddate() {
        return enddate;
    }

    public List<Match> getMatches() {
        return matches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Round round1 = (Round) o;

        if (!round.equals(round1.round)) return false;
        return enddate.equals(round1.enddate);

    }

    @Override
    public int hashCode() {
        int result = round.hashCode();
        result = 31 * result + enddate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Round{" +
                "round='" + round + '\'' +
                ", title='" + title + '\'' +
                ", fromto='" + fromto + '\'' +
                ", active='" + active + '\'' +
                ", enddate='" + enddate + '\'' +
                ", matches=" + matches +
                '}';
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
