package de.olivergeisel.kegelbroker.client.flat;

import core.game.Game;
import core.team_and_player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("flat")
class PlayerFlatTest {

	@Test
	void copiesPlayerFieldsAndUsesTheProvidedFlatGame() {
		var source = new Player<Game>("Ada", "Lovelace", "Analytical Club", "North", LocalDate.of(2000, 1, 1));
		var game = mock(Game.class);
		source.setGame(game);
		var flatGame = mock(GameFlat.class);

		var flat = new PlayerFlat(source, flatGame);

		assertAll("flattened player",
				() -> assertEquals("Ada", flat.getVorname(), "The first name should be copied"),
				() -> assertEquals("Lovelace", flat.getNachname(), "The last name should be copied"),
				() -> assertEquals("Ada Lovelace", flat.getCompleteName(), "The complete name should be preserved"),
				() -> assertEquals("Analytical Club", flat.getClub(), "The club should be copied"),
				() -> assertEquals("North", flat.getTeam(), "The team name should be copied"),
				() -> assertEquals(LocalDate.of(2000, 1, 1), flat.getBirthday(), "The birthday should be copied"),
				() -> assertSame(flatGame, flat.getGame(), "The original game should be replaced by its flat version")
		);
	}
}
