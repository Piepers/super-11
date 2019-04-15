package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimalistic representation of a team in the competition, mainly to determine whether a match is currently being
 * played.
 *
 * @author Bas Piepers
 */
@DataObject
public class Team implements JsonDomainObject {
    private final String id;
    private final String name;


    private Team(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Team(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
    }

    public static Team of(String id, String name) {
        return new Team(id, name);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Team team = (Team) o;

        return id.equals(team.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
