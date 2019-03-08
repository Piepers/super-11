package me.piepers.super11.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * Represents the data structure that comes back from the "competitions" API call and contains all the information that
 * we need to display the standings of our league.
 *
 * @author Bas Piepers
 */
@DataObject
public class Competition implements JsonDomainObject {
    private final String version;
    private final Boolean error;
    private final String message;
    private final String status;
    private CompetitionData data;

    public Competition(JsonObject jsonObject) {
        this.version = jsonObject.getString("version");
        this.error = jsonObject.getBoolean("error");
        this.message = jsonObject.getString("message");
        this.status = jsonObject.getString("status");
        this.data = Objects.nonNull(jsonObject.getJsonObject("data")) ? new CompetitionData(jsonObject.getJsonObject("data")) : null;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public CompetitionData getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Competition{" +
                "version='" + version + '\'' +
                ", error=" + error +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
