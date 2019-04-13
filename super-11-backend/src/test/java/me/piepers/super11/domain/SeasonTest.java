package me.piepers.super11.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.TestHelper;
import me.piepers.super11.infrastructure.model.EredivisieSeason;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SeasonTest {
    @Test
    public void test_that_eredivisie_season_is_mapped_as_expected() throws IOException {
        // Given
        String json = TestHelper.convertJsonFileToString("matches-response.json");
        JsonArray jsonArray = new JsonArray(json);
        JsonObject jsonObject = new JsonObject().put("rounds", jsonArray);
        EredivisieSeason eredivisieSeason = new EredivisieSeason(jsonObject);

        // When
        Season season = Season.from(eredivisieSeason);

        // Then
        assertThat(season.getCountry()).isEqualTo("NL");
        assertThat(season.getName()).isEqualTo("Eredivisie");
        assertThat(season.getRounds()).isNotEmpty();
        assertThat(season.getRounds().size()).isEqualTo(eredivisieSeason.getRounds().size());
    }
}
