package me.piepers.super11.infrastructure.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SeasonTest {
    @Test
    public void test_that_response_is_mapped_with_jsonobject_constructor_as_expected() throws Exception {
        // Given
        Instant now = Instant.now();
        String json = this.convertJsonFileToString();
        JsonArray jsonArray = new JsonArray(json);
        JsonObject jsonObject = new JsonObject().put("rounds", jsonArray).put("lastChecked", now);

        // When
        Season season = new Season(jsonObject);

        // Then
        this.assertSeason(season);
    }

    private String convertJsonFileToString() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("matches-response.json");
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));

        }
    }

    private void assertSeason(Season season) {
        assertThat(season).isNotNull();
        assertThat(season.getRounds()).isNotEmpty();
        List<Round> rounds = season.getRounds();
        assertThat(rounds.size()).isEqualTo(34);
        rounds
                .stream()
                .forEach(round -> {
                    // Just some simple asserts so that we know that at least something has been put into the fields.
                    assertThat(round.getMatches()).isNotEmpty();
                    assertThat(round.getRound()).isNotEmpty();
                    assertThat(round.getFromto()).isNotEmpty();
                    assertThat(round.getEnddate()).isNotEmpty();
                    round.getMatches().stream().forEach(match -> {
                        assertThat(match.getGameId()).isNotEmpty();
                    });
                });
    }
}
