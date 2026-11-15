package de.olivergeisel.kegelbroker.client.flat;

import core.game.Game;
import core.match.Match;
import core.team_and_player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;

@Tag("unit")
@Tag("flat")
class MatchFlattenerTest {

	@Test
	void flattensTeamsAndRanksPlayersByDescendingScore() {
		var lowerScoringPlayer = FlatTestSupport.player("Grace", "Hopper", 45);
		var topScoringPlayer = FlatTestSupport.player("Ada", "Lovelace", 90);
		var middleScoringPlayer = FlatTestSupport.player("Katherine", "Johnson", 70);
		var team = FlatTestSupport.team("North", lowerScoringPlayer, topScoringPlayer, middleScoringPlayer);
		var match = FlatTestSupport.match(team);
		var flattener = new MatchFlattener<Game>();
		@SuppressWarnings("unchecked")
		var matchView = (Match<Game>) match;

		var flat = flattener.flat(matchView);
		@SuppressWarnings("unchecked")
		var rankedPlayers = ((Map<String, List<String>>) flat.getExtra()).get("North");

		assertAll("flattened match and ranking",
				() -> assertEquals(List.of("Ada Lovelace", "Katherine Johnson", "Grace Hopper"), rankedPlayers,
						"The extra player list should be sorted from highest to lowest score"),
				() -> assertSame(match.getStatusInfo(), flat.getStatusInfo(),
						"The source match status should be retained by the flat match")
		);
	}

	@Test
	void preservesAnEmptyMatchWithoutTeams() {
		var match = FlatTestSupport.match();
		var flattener = new MatchFlattener<Game>();

		var flat = flattener.flat(match);

		assertAll("empty flattened match",
				() -> assertEquals(Map.of(), flat.getExtra(), "An empty match should produce no ranking data")
		);
	}
}
