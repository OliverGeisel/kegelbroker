package de.olivergeisel.kegelbroker.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@Tag("model")
class LiveMatchTest {

	@Test
	void constructorKeepsProvidedValuesAndInitializesCollections() {
		var date = LocalDate.of(2026, 11, 7);
		var match = new LiveMatch(date, "Finale", "finale-dir", MatchType.FINALE, false, true, true);

		assertAll("new match values",
				() -> assertEquals(date, match.getDate(), "The match date should be preserved"),
				() -> assertEquals("Finale", match.getMatchName(), "The match name should be preserved"),
				() -> assertEquals("finale-dir", match.getMatchDir(), "The directory name should be preserved"),
				() -> assertEquals(MatchType.FINALE, match.getMatchType(), "The match type should be preserved"),
				() -> assertFalse(match.getRunning(), "The running state should be initialized from the constructor"),
				() -> assertTrue(match.getStatic(), "The static state should be initialized from the constructor"),
				() -> assertTrue(match.getSpecial(), "The special state should be initialized from the constructor"),
				() -> assertTrue(match.getTeams().isEmpty(), "A new match should have no teams"),
				() -> assertTrue(match.getMatchTree().isEmpty(), "A new match should have no match trees"),
				() -> assertNull(match.getId(), "A new match should not have a database id before persistence"),
				() -> assertEquals("", match.getTeamsAsString(), "An empty team list should format as an empty string")
		);
	}

	@Test
	void defaultsAndStateTransitionsAreConsistent() {
		var match = new LiveMatch(LocalDate.of(2026, 11, 7), "League", "league-dir", MatchType.VORLAUF,
				true, false, false);

		assertAll("default match state",
				() -> assertTrue(match.getRunning(), "Matches should run by default"),
				() -> assertFalse(match.getStatic(), "Matches should be updatable by default"),
				() -> assertFalse(match.getSpecial(), "Matches should not be special by default"),
				() -> assertFalse(match.isEnded(), "A running match should not be ended")
		);

		match.endMatch();
		assertTrue(match.isEnded(), "Ending a match should clear its running state");
		match.setRunning();
		assertTrue(match.getRunning(), "setRunning should mark a match as running");
		match.setStatic();
		assertTrue(match.getStatic(), "setStatic should make a match static");
		match.setUpdating();

		assertAll("updated match state",
				() -> assertFalse(match.getStatic(), "setUpdating should allow a match to be updated"),
				() -> assertTrue(match.getRunning(), "Changing update mode should not end the match")
		);
	}

	@Test
	void teamsAsStringUsesCommaAndSpaceSeparators() {
		var match = new LiveMatch(LocalDate.of(2026, 11, 7), "League", "league-dir", MatchType.VORLAUF,
				true, false, false);
		match.setTeams(java.util.List.of("Team A", "Team B", "Team C"));

		assertEquals("Team A, Team B, Team C", match.getTeamsAsString(),
				"Team names should be joined in their existing order");
	}
}
