package me.piepers.super11.infrastructure.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EredivisieSeasonTest {
    @Test
    public void test_that_response_is_mapped_with_jsonobject_constructor_as_expected() throws Exception {
        // Given
        String json = TestHelper.convertJsonFileToString("matches-response.json");
        JsonArray jsonArray = new JsonArray(json);
        JsonObject jsonObject = new JsonObject().put("rounds", jsonArray);

        // When
        EredivisieSeason season = new EredivisieSeason(jsonObject);

        // Then
        this.assertSeason(season);
    }

    private void assertSeason(EredivisieSeason season) {
        assertThat(season).isNotNull();
        assertThat(season.getRounds()).isNotEmpty();
        List<EredivisieRound> rounds = season.getRounds();
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
