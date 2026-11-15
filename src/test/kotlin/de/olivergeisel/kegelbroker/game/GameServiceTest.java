package de.olivergeisel.kegelbroker.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@Tag("unit")
@Tag("service")
class GameServiceTest {

	private GameService gameService;

	@BeforeEach
	void setUp() {
		gameService = new GameService();
	}

	@Test
	void getGameReturnsAnOverview() {
		var overview = gameService.getGame();

		assertNotNull(overview, "getGame should return a game overview");
	}

	@Test
	void eachGetGameCallReturnsANewOverview() {
		var firstOverview = gameService.getGame();
		var secondOverview = gameService.getGame();

		assertNotSame(firstOverview, secondOverview,
				"Each getGame call should return a separate overview instance");
	}
}
