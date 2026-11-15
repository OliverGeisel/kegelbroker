package de.olivergeisel.kegelbroker.client.flat;

import core.game.Game;
import core.team_and_player.Team;
import core.team_and_player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("flat")
class MatchFlatTest {

	@Test
	void constructorExposesFlatMatchDataWithoutCyclicStructures() throws ReflectiveOperationException {
		var player = FlatTestSupport.player("Ada", "Lovelace", 82);
		var team = FlatTestSupport.team("North", player);
		var match = FlatTestSupport.match(team);
		var statusInfo = match.getStatusInfo();
		when(statusInfo.isFinished()).thenReturn(true);
		when(statusInfo.isAborted()).thenReturn(false);
		doReturn(List.of(player)).when(match).getCurrentPlayers();
		doReturn(List.of(List.of(player.getGame()))).when(match).getGameRounds();
		var teamFlat = new TeamFlat<>(team);
		var points = match.getPoints();
		var setPoints = match.getSetPoints();
		var extra = Map.of("North", List.of("Ada Lovelace"));

		var flat = new MatchFlat<>(match, List.of(teamFlat), extra);
		var flattenedTeams = exposedTeams(flat);

		assertAll("flattened match",
				() -> assertEquals(1, flattenedTeams.length, "The flattened match should expose its teams"),
				() -> assertSame(teamFlat, flattenedTeams[0], "The supplied flat team should be retained"),
				() -> assertTrue(flat.getGames().isEmpty(), "The flattened match should not expose nested games"),
				() -> assertSame(points, flat.getPoints(), "Match points should be preserved"),
				() -> assertSame(setPoints, flat.getSetPoints(), "Set points should be preserved"),
				() -> assertTrue(flat.getFinished(), "The finished status should be copied"),
				() -> assertFalse(flat.getAborted(), "The aborted status should be copied"),
				() -> assertTrue(flat.getFinal(), "A finished match should be marked final"),
				() -> assertEquals(1, flat.getCurrentPlayers().size(),
						"Current players should be represented as flat players"),
				() -> assertEquals("Ada Lovelace", flat.getCurrentPlayers().get(0).getCompleteName(),
						"Current player details should be retained"),
				() -> assertEquals(1, flat.getGameRounds().size(), "Each source game round should be retained"),
				() -> assertEquals(1, flat.getGameRounds().get(0).size(),
						"Each game in a source round should be represented"),
				() -> assertTrue(flat.getPlayerForSet().isEmpty(),
						"Set-player mapping should not contain cyclic references"),
				() -> assertTrue(flat.getGamesByPlayerPos().isEmpty(),
						"Player-position games should not be exposed in the flat match"),
				() -> assertEquals(extra, flat.getExtra(), "Additional flattened data should be retained")
		);
	}

	@SuppressWarnings("unchecked")
	private static Team<GameFlat>[] exposedTeams(MatchFlat<Game> flat) throws ReflectiveOperationException {
		var teamsMethod = Arrays.stream(flat.getClass().getMethods())
				.filter(method -> method.getName().equals("getTeams") && method.getReturnType().isArray())
				.findFirst()
				.orElseThrow(() -> new NoSuchMethodException("Array-returning getTeams method was not found"));
		return (Team<GameFlat>[]) teamsMethod.invoke(flat);
	}
}
