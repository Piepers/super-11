package me.piepers.super11.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.piepers.super11.TestHelper;
import me.piepers.super11.infrastructure.model.EredivisieSeason;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

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

    @Test
    public void test_that_when_time_is_at_active_round_and_active_match_that_season_indicates_that_match_is_active() throws IOException {
        // Given
        ZonedDateTime zdt = ZonedDateTime.of(2019, 4, 14, 16, 00, 00, 00, ZoneId.of("Europe/Amsterdam"));
        Instant inRound30 = zdt.toInstant();
        Season season = this.convertFileToSeason();

        // When
        boolean isActive = season.isMatchActiveAt(inRound30);

        // Then
        assertThat(isActive).isTrue();
    }

    @Test
    public void test_that_when_time_is_in_active_round_but_not_at_active_match_that_season_indicates_that_match_is_not_active() throws IOException {
        // Given
        ZonedDateTime zdt = ZonedDateTime.of(2019, 4, 14, 16, 44, 00, 00, ZoneId.of("Europe/Amsterdam"));
        // The time is in round 30 but there is no active match at that time.
        Instant inRound30 = zdt.toInstant();
        Season season = this.convertFileToSeason();

        // When
        boolean isActive = season.isMatchActiveAt(inRound30);

        // Then
        assertThat(isActive).isFalse();
    }

    @Test
    public void test_that_when_time_is_not_at_active_round_that_season_indicates_that_match_is_not_active() throws IOException {
        // Given
        ZonedDateTime zdt = ZonedDateTime.of(2019, 4, 12, 13, 10, 00, 00, ZoneId.of("Europe/Amsterdam"));
        // The time is in round 30 but there is no active match at that time.
        Instant notInRound = zdt.toInstant();
        Season season = this.convertFileToSeason();

        // When
        boolean isActive = season.isMatchActiveAt(notInRound);

        // Then
        assertThat(isActive).isFalse();
    }

    @Test
    public void test_that_when_time_is_at_active_round_that_season_indicates_expected_matches_are_currently_active() throws IOException {
        // Given
        ZonedDateTime zdt = ZonedDateTime.of(2019, 4, 14, 17, 00, 00, 00, ZoneId.of("Europe/Amsterdam"));
        Instant round30 = zdt.toInstant();
        Season season = this.convertFileToSeason();

        // When
        List<Match> activeMatches = season.whichMatchesAreActiveAt(round30);

        // Then
        assertThat(activeMatches.size()).isEqualTo(1);
        Match activeMatch = activeMatches.get(0);
        assertThat(activeMatch.getHome()).isEqualTo(Team.of("t325", "FC Utrecht"));
        assertThat(activeMatch.getAway()).isEqualTo(Team.of("t232", "Vitesse"));
    }

    @Test
    public void test_that_when_time_is_at_active_round_with_multiple_matches_that_season_indicates_the_expected_amount_of_matches() throws IOException {
        // Given
        ZonedDateTime zdt = ZonedDateTime.of(2019, 4, 14, 16, 00, 00, 00, ZoneId.of("Europe/Amsterdam"));
        Instant round30 = zdt.toInstant();
        Season season = this.convertFileToSeason();

        // When
        List<Match> activeMatches = season.whichMatchesAreActiveAt(round30);

        // Then
        assertThat(activeMatches.size()).isEqualTo(2);
        Match pecVsWillemII = Match.of(Team.of("t207", "Willem II"), Team.of("t424", "PEC Zwolle"), Instant.parse("2019-04-14T12:30:00Z"));
        Match heerenveenVsGroningen = Match.of(Team.of("t318", "sc Heerenveen"), Team.of("t425", "FC Groningen"), Instant.parse("2019-04-14T12:30:00Z"));
        assertThat(activeMatches).containsExactlyInAnyOrder(pecVsWillemII, heerenveenVsGroningen);
    }

    @Test
    public void test_that_when_time_is_not_at_active_round_that_season_indicates_no_matches_are_currently_active() throws IOException {
        // Given
        ZonedDateTime zdt = ZonedDateTime.of(2019, 4, 14, 14, 15, 00, 00, ZoneId.of("Europe/Amsterdam"));
        Instant round30 = zdt.toInstant();
        Season season = this.convertFileToSeason();

        // When
        List<Match> activeMatches = season.whichMatchesAreActiveAt(round30);

        // Then
        assertThat(activeMatches.size()).isEqualTo(0);
    }

    private Season convertFileToSeason() throws IOException {
        String json = TestHelper.convertJsonFileToString("matches-response.json");
        JsonArray jsonArray = new JsonArray(json);
        JsonObject jsonObject = new JsonObject().put("rounds", jsonArray);
        EredivisieSeason eredivisieSeason = new EredivisieSeason(jsonObject);
        return Season.from(eredivisieSeason);
    }
}
