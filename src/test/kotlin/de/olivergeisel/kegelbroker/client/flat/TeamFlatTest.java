package de.olivergeisel.kegelbroker.client.flat;

import core.game.Game;
import core.team_and_player.GeneralTeamInfo;
import core.team_and_player.Player;
import core.team_and_player.Team;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("flat")
class TeamFlatTest {

	@Test
	void constructorFlattensTeamAndItsPlayers() {
		var player = FlatTestSupport.player("Ada", "Lovelace", 75);
		var team = FlatTestSupport.team("North", player);
		var teamInfo = team.getGeneralTeamInfo();

		var flat = new TeamFlat<>(team);
		var flatPlayers = flat.getPlayers();

		assertAll("flattened team",
				() -> assertEquals("North", flat.getName(), "The team name should be retained"),
				() -> assertSame(teamInfo, flat.getGeneralTeamInfo(), "General team information should be retained"),
				() -> assertEquals(1, flatPlayers.length, "Every source player should be included"),
				() -> assertEquals("Ada Lovelace", flatPlayers[0].getCompleteName(),
						"The flattened player should retain its complete name"),
				() -> assertEquals(75, flatPlayers[0].getGame().getTotalScore(),
						"The flattened player's game should retain its score")
		);
	}

	@Test
	void nameCanBeUpdatedButCannotBeSetToNull() {
		var team = FlatTestSupport.team("North");
		var flat = new TeamFlat<>(team);
		flat.setName("South");

		assertEquals("South", flat.getName(), "The name setter should update the flattened team");
		assertThrows(NullPointerException.class, () -> flat.setName(null),
				"A null team name should be rejected");
	}
}
