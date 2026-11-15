package de.olivergeisel.kegelbroker.client.flat;

import core.game.Game;
import core.game.GameInfo;
import core.game.GameKind;
import core.game.GameSet;
import core.team_and_player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("flat")
class GameFlatTest {

	@Test
	void constructorAndComputedValuesRepresentTheSourceGame() {
		var sourceGame = mock(Game.class);
		var currentPlayer = mock(Player.class);
		var date = LocalDateTime.of(2026, 11, 7, 12, 30);
		var firstSet = mock(GameSet.class);
		var secondSet = mock(GameSet.class);
		var gameInfo = new GameInfo(2, 1, 1, 10.0, true);
		var gameKind = GameKind.GAME_120;
		when(sourceGame.getDate()).thenReturn(date);
		when(sourceGame.getCurrentPlayer()).thenReturn(currentPlayer);
		when(currentPlayer.getCompleteName()).thenReturn("Ada Lovelace");
		when(sourceGame.getGameSets()).thenReturn(new GameSet[]{firstSet, secondSet});
		when(sourceGame.getGameInfo()).thenReturn(gameInfo);
		when(sourceGame.getGameKind()).thenReturn(gameKind);
		when(firstSet.getScore()).thenReturn(45);
		when(secondSet.getScore()).thenReturn(52);
		when(firstSet.getVolleScore()).thenReturn(30);
		when(secondSet.getVolleScore()).thenReturn(35);
		when(firstSet.getAbraeumenScore()).thenReturn(15);
		when(secondSet.getAbraeumenScore()).thenReturn(17);
		when(firstSet.getAnzahlFehler()).thenReturn(1);
		when(secondSet.getAnzahlFehler()).thenReturn(2);
		when(firstSet.getAnzahlGespielteWuerfe()).thenReturn(10);
		when(secondSet.getAnzahlGespielteWuerfe()).thenReturn(11);

		var flat = new GameFlat(sourceGame);

		assertAll("flattened game",
				() -> assertEquals(date, flat.getDate(), "The flattened game should retain its date"),
				() -> assertEquals(2, flat.getNumberOfGameSets(), "The flattened game should retain its set count"),
				() -> assertSame(firstSet, flat.getGameSet(0), "Set lookup should return the matching game set"),
				() -> assertEquals(97, flat.getTotalScore(), "Total score should sum all game-set scores"),
				() -> assertEquals(65, flat.getTotalVolle(), "Full-pin score should sum all game sets"),
				() -> assertEquals(32, flat.getTotalAbraeumen(), "Clearing score should sum all game sets"),
				() -> assertEquals(3, flat.getTotalFehlwurf(), "Missed throws should sum all game sets"),
				() -> assertEquals(21, flat.getNumberOfWurf(), "Throw count should sum all game sets"),
				() -> assertSame(gameInfo, flat.getGameInfo(), "Game information should be retained"),
				() -> assertSame(gameKind, flat.getGameKind(), "Game kind should be retained"),
				() -> assertNull(flat.getPlayer(), "Flat games should not expose a cyclic player reference")
		);
	}

	@Test
	void supportsReplacingGameSetsAndRejectsAnInvalidIndex() {
		var flat = new GameFlat(FlatTestSupport.player("Ada", "Lovelace", 42).getGame());
		var set = mock(GameSet.class);
		flat.setGameSets(List.of(set));

		assertAll("replaced game sets",
				() -> assertEquals(1, flat.getNumberOfGameSets(), "Replacing sets should update the set count"),
				() -> assertSame(set, flat.getGameSet(0), "Replacing sets should update indexed access")
		);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> flat.getGameSet(1),
				"An index past the final set should be rejected");
		assertThrows(NullPointerException.class, () -> flat.setGameSets(null),
				"Null game-set collections should be rejected");
	}
}
