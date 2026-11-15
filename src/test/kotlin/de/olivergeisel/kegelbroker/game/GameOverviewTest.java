package de.olivergeisel.kegelbroker.game;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@Tag("model")
class GameOverviewTest {

	@Test
	void constructorCreatesAnOverview() {
		var overview = new GameOverview();

		assertNotNull(overview, "The default constructor should create a game overview");
	}
}
