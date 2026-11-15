package de.olivergeisel.kegelbroker.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@Tag("form")
class MatchCreateFormTest {

	@Test
	void constructorPreservesAllFormValues() {
		var matchDate = LocalDate.of(2026, 11, 7);
		var form = new MatchCreateForm("Vorlauf", matchDate, "vorlauf-1", "120-points",
				"https://example.test/match", true, MatchType.VORLAUF);

		assertAll("match creation form",
				() -> assertEquals("Vorlauf", form.getMatchName(), "The form should retain the match name"),
				() -> assertEquals(matchDate, form.getMatchDate(), "The form should retain the match date"),
				() -> assertEquals("vorlauf-1", form.getMatchId(), "The form should retain the local match id"),
				() -> assertEquals("120-points", form.getPointSystem(), "The form should retain the point system"),
				() -> assertEquals("https://example.test/match", form.getUrl(), "The form should retain the source URL"),
				() -> assertTrue(form.getLocalServer(), "The local server option should be retained"),
				() -> assertEquals(MatchType.VORLAUF, form.getMatchType(), "The form should retain the match type")
		);
	}

	@Test
	void mutableFormValuesCanBeUpdated() {
		var form = new MatchCreateForm("Old name", LocalDate.of(2026, 11, 7), "old-id", "old-system",
				"", false, MatchType.FINALE);
		var newDate = LocalDate.of(2026, 11, 8);

		form.setMatchName("New name");
		form.setMatchDate(newDate);
		form.setMatchId("new-id");
		form.setPointSystem("new-system");
		form.setUrl("https://example.test/new-match");
		form.setLocalServer(true);
		form.setMatchType(MatchType.HALBFINALE);

		assertAll("updated form values",
				() -> assertEquals("New name", form.getMatchName(), "The match name setter should update the form"),
				() -> assertEquals(newDate, form.getMatchDate(), "The date setter should update the form"),
				() -> assertEquals("new-id", form.getMatchId(), "The id setter should update the form"),
				() -> assertEquals("new-system", form.getPointSystem(), "The point-system setter should update the form"),
				() -> assertEquals("https://example.test/new-match", form.getUrl(), "The URL setter should update the form"),
				() -> assertTrue(form.getLocalServer(), "The local-server setter should update the form"),
				() -> assertEquals(MatchType.HALBFINALE, form.getMatchType(), "The match-type setter should update the form")
		);
		form.setLocalServer(false);
		assertFalse(form.getLocalServer(), "The local-server option should also be switchable off");
	}
}
