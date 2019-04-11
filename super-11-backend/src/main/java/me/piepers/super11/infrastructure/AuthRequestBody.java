package me.piepers.super11.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.domain.JsonDomainObject;

import java.util.Base64;

/**
 * Helper class that represents the body that is issued with the first request of the OAuth flow.
 *
 * @author Bas Piepers
 */
@DataObject
public class AuthRequestBody implements JsonDomainObject {
    @JsonProperty("Email")
    private final String email;
    @JsonProperty("Password")
    private final String password;
    @JsonProperty("Persist")
    private final Boolean persist;
    @JsonProperty("Destination")
    private final String destination;
    @JsonProperty("Af")
    private final String af;
    @JsonProperty("GoogleRecaptchaResponse")
    private final String googleRecaptchaResponse;
    @JsonProperty("UserType")
    private final Integer userType;

    public AuthRequestBody(JsonObject jsonObject) {
        this.email = jsonObject.getString("Email");
        this.password = jsonObject.getString("Password");
        this.persist = jsonObject.getBoolean("Persist");
        this.destination = jsonObject.getString("Destination");
        this.af = jsonObject.getString("Af");
        this.googleRecaptchaResponse = jsonObject.getString("GoogleRecaptchaResponse");
        this.userType = jsonObject.getInteger("UserType");
    }

    private AuthRequestBody(String email, String password, Boolean persist, String destination, String af, String googleRecaptchaResponse, Integer userType) {
        this.email = email;
        this.password = password;
        this.persist = persist;
        this.destination = destination;
        this.af = af;
        this.googleRecaptchaResponse = googleRecaptchaResponse;
        this.userType = userType;
    }

    public static AuthRequestBody fromConfiguration(JsonObject configuration) {
        String email = configuration.getString("email");
        String password = new String(Base64.getDecoder().decode(configuration.getString("password")));
        Boolean persist = configuration.getBoolean("persist");
        String destinationHost = configuration.getString("destination_host");
        Integer destinationPort = configuration.getInteger("destination_port");
        String destinationUrl = configuration.getString("destination_url");
        String clientId = configuration.getString("client_id");
        String redirectUrl = configuration.getString("redirect_uri");
        String responseType = configuration.getString("response_type");
        String af = configuration.getString("af");
        String googleRecatchaResponse = configuration.getString("google_recaptcha_response");
        Integer userType = configuration.getInteger("userType");
        String formattedDestination = "https://" + destinationHost + ":" + destinationPort + destinationUrl + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=" + responseType;
        return new AuthRequestBody(email, password, persist, formattedDestination, af, googleRecatchaResponse, userType);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getPersist() {
        return persist;
    }

    public String getDestination() {
        return destination;
    }

    public String getAf() {
        return af;
    }

    public String getGoogleRecaptchaResponse() {
        return googleRecaptchaResponse;
    }

    public Integer getUserType() {
        return userType;
    }

    @Override
    public String toString() {
        return "AuthRequestBody{" +
                "email='" + email + '\'' +
                ", persist=" + persist +
                ", destination='" + destination + '\'' +
                ", af='" + af + '\'' +
                ", googleRecaptchaResponse='" + googleRecaptchaResponse + '\'' +
                ", userType=" + userType +
                '}';
    }
}
