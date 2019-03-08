package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The data portion of the competition API call that contains the standings of our competition (the sub-league).
 *
 * @author Bas Piepers
 */
@DataObject
public class CompetitionData implements JsonDomainObject {
    private final String id;
    private final String name;
    private String token;
    private List<FieldValue> periods;
    private List<FieldValue> keys;
    private List<Draft> drafts;
    private MetaData draftsMetadata;

    public CompetitionData(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.token = jsonObject.getString("token");
        this.periods = jsonObject.getJsonArray("periods", new JsonArray())
                .stream()
                .map(o -> (FieldValue.of(((JsonObject) o).getString("field"),
                        ((JsonObject) o).getString("value"))))
                .collect(Collectors.toList());
        this.keys = jsonObject.getJsonArray("keys", new JsonArray())
                .stream()
                .map(o -> (FieldValue.of(((JsonObject) o).getString("field"),
                        ((JsonObject) o).getString("value"))))
                .collect(Collectors.toList());
        this.drafts = jsonObject.getJsonArray("drafts", new JsonArray())
                .stream()
                .map(o -> new Draft((JsonObject) o))
                .collect(Collectors.toList());
        this.draftsMetadata = new MetaData(jsonObject.getJsonObject("draftsMetadata"));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public List<FieldValue> getPeriods() {
        return periods;
    }

    public List<FieldValue> getKeys() {
        return keys;
    }

    public List<Draft> getDrafts() {
        return drafts;
    }

    public MetaData getDraftsMetadata() {
        return draftsMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompetitionData that = (CompetitionData) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "CompetitionData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", periods=" + periods +
                ", keys=" + keys +
                ", drafts=" + drafts +
                ", draftsMetadata=" + draftsMetadata +
                '}';
    }
}
