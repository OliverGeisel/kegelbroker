package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.game.GameKind;
import core.match.Match;
import de.olivergeisel.kegelbroker.client.flat.MatchFlat;
import de.olivergeisel.kegelbroker.client.flat.MatchFlattener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("controller")
class LiveMatchControllerTest {

	private LiveMatchRepository  liveMatchRepository;
	private MatchFlattener<Game> matchFlattener;
	private LocalMatchService    localMatchService;
	private LiveMatchController  controller;

	@BeforeEach
	void setUp() {
		liveMatchRepository = mock(LiveMatchRepository.class);
		matchFlattener = mock(MatchFlattener.class);
		localMatchService = mock(LocalMatchService.class);
		controller = new LiveMatchController(liveMatchRepository, matchFlattener, localMatchService);
	}

	@Test
	void overviewSeparatesRunningFromCompletedAndStaticMatches() {
		var day = LocalDate.of(2026, 11, 7);
		var running = newMatch("running", true, false);
		var staticMatch = newMatch("static-running", true, true);
		var finished = newMatch("finished", false, false);
		var matches = org.springframework.data.util.Streamable.of(running, staticMatch, finished);
		when(localMatchService.getAllMatchesOf(day)).thenReturn(matches);
		var model = new ExtendedModelMap();

		var view = controller.overview(day, model);
		var liveMatches = ((org.springframework.data.util.Streamable<LiveMatch>) model.get("liveMatches"))
				.stream().toList();
		var completedMatches = ((org.springframework.data.util.Streamable<LiveMatch>) model.get("completedMatches"))
				.stream().toList();

		assertAll("overview model",
				() -> assertEquals("matchDisplay/overview", view, "The overview should use its view template"),
				() -> assertEquals(List.of(running), liveMatches,
						"Static matches should not be listed as live"),
				() -> assertEquals(List.of(staticMatch, finished), completedMatches,
						"Static or ended matches should be listed as completed"),
				() -> assertEquals(Set.of(MatchType.values()), Set.copyOf((Set<MatchType>) model.get("templates")),
						"The overview should expose templates for all match types")
		);
		verify(localMatchService).getAllMatchesOf(day);
	}

	@Test
	void overviewWithoutDayUsesTodaysMatches() {
		var model = new ExtendedModelMap();
		when(localMatchService.getAllMatchesToday()).thenReturn(org.springframework.data.util.Streamable.empty());

		var view = controller.overview(null, model);

		assertEquals("matchDisplay/overview", view, "The default overview should use its view template");
		verify(localMatchService).getAllMatchesToday();
		verify(localMatchService, never()).getAllMatchesOf(any(LocalDate.class));
	}

	@Test
	void editSelectListsAllMatchesInDateAndNameOrder() {
		var older = newMatch("Zed", true, false);
		var newer = new LiveMatch(LocalDate.of(2026, 11, 8), "Alpha", "Alpha", MatchType.FINALE,
				true, false, false);
		when(liveMatchRepository.findAll()).thenReturn(org.springframework.data.util.Streamable.of(older, newer));
		var model = new ExtendedModelMap();

		var view = controller.editSelect(model);

		assertAll("edit selection",
				() -> assertEquals("matchDisplay/edit-select", view),
				() -> assertEquals(List.of(newer, older), model.get("matches"))
		);
	}

	@Test
	void editMatchPassesTheStoredMatchAndAvailableTypesToTheTemplate() {
		var match = newMatch("event", true, false);
		var model = new ExtendedModelMap();
		when(liveMatchRepository.findById(match.getId())).thenReturn(java.util.Optional.of(match));

		var view = controller.editMatch(match.getId(), model, new RedirectAttributesModelMap());

		assertAll("edit match model",
				() -> assertEquals("matchDisplay/edit", view),
				() -> assertSame(match, model.get("match")),
				() -> assertEquals(List.of(MatchType.values()), model.get("matchTypes"))
		);
		verifyNoInteractions(localMatchService);
	}

	@Test
	@Disabled("The date is fixed provide testdata first then enable this test")
	void updateMatchDelegatesAllSubmittedFieldsAndRedirectsToTheRenamedMatch() {
		var redirectAttributes = new RedirectAttributesModelMap();
		var date = LocalDate.of(2026, 11, 8);
		var matchId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		when(liveMatchRepository.findById(matchId)).thenReturn(java.util.Optional.of(newMatch("old-name", true, false)));

		var view = controller.updateMatch(matchId, "new-name", date, "match-directory", MatchType.VORLAUF,
				false, true, true, redirectAttributes);

		assertAll("update match",
				() -> assertEquals("redirect:/live/edit", view),
				() -> assertEquals("new-name", redirectAttributes.get("matchId")),
				() -> assertEquals("Match wurde gespeichert.", redirectAttributes.getFlashAttributes().get("message"))
		);
		verify(localMatchService).updateMatch(matchId, "new-name", date, "match-directory", MatchType.VORLAUF,
				false, true, true);
	}

	@Test
	void displayMatchReturnsEmptyViewAndErrorWhenMatchDoesNotExist() {
		var model = new ExtendedModelMap();
		when(liveMatchRepository.findByMatchNameIgnoreCase("missing")).thenReturn(null);

		var view = controller.displayMatch("missing", "NOT_A_TYPE", model);

		assertAll("missing match display",
				() -> assertEquals("matchDisplay/empty", view, "Unknown or missing matches should use the empty view"),
				() -> assertEquals("Match not found. Nice Try! ;)", model.get("error"),
						"The model should explain that the requested match was not found")
		);
		verifyNoInteractions(localMatchService);
	}

	@Test
	void displayMatchUsesTheTemplateForTheRequestedType() {
		var match = newMatch("Finale", true, false);
		var cachedMatch = mockMatch();
		var model = new ExtendedModelMap();
		when(liveMatchRepository.findByMatchNameIgnoreCase("Finale")).thenReturn(match);
		doReturn(cachedMatch).when(localMatchService).getMatchCached("Finale");

		var view = controller.displayMatch("Finale", MatchType.FINALE.name(), model);

		assertAll("finale display",
				() -> assertEquals("matchDisplay/finale", view, "Finale matches should use the finale view"),
				() -> assertSame(cachedMatch, model.get("match"), "The cached match should be exposed to the view"),
				() -> assertEquals("Finale", model.get("matchName"), "The requested name should be exposed to the view")
		);
	}

	@Test
	void twentyFourHourViewHandlesMissingAndExistingMatches() {
		var missingModel = new ExtendedModelMap();
		when(liveMatchRepository.findByMatchNameIgnoreCase("missing")).thenReturn(null);
		var missingView = controller.get24Hours("missing", missingModel);

		var match = newMatch("event", true, false);
		var cachedMatch = mockMatch();
		var foundModel = new ExtendedModelMap();
		when(liveMatchRepository.findByMatchNameIgnoreCase("event")).thenReturn(match);
		doReturn(cachedMatch).when(localMatchService).getMatchCached("event");
		var foundView = controller.get24Hours("event", foundModel);

		assertAll("24-hour view",
				() -> assertEquals("matchDisplay/empty", missingView, "Missing matches should use the empty view"),
				() -> assertEquals("Match not found. Nice Try! ;)", missingModel.get("error"),
						"Missing matches should produce a useful error model attribute"),
				() -> assertEquals("matchDisplay/24-hours", foundView, "Found matches should use the 24-hour view"),
				() -> assertSame(cachedMatch, foundModel.get("match"),
						"The cached match should be passed to the 24-hour view"),
				() -> assertEquals("event", foundModel.get("matchName"),
						"The requested match name should be passed to the 24-hour view")
		);
		verify(localMatchService, never()).getMatchCached("missing");
	}

	@Test
	void preliminaryDisplaySelectsSprintAndStandardViewsFromGameKind() {
		var sprintMatch = mockMatch();
		var sprintConfig = mock(core.match.MatchConfig.class);
		when(sprintMatch.getConfig()).thenReturn(sprintConfig);
		when(sprintConfig.getKind()).thenReturn(GameKind.GAME_40);
		var standardMatch = mockMatch();
		var standardConfig = mock(core.match.MatchConfig.class);
		when(standardMatch.getConfig()).thenReturn(standardConfig);
		when(standardConfig.getKind()).thenReturn(GameKind.GAME_120);
		when(liveMatchRepository.findByMatchName("sprint")).thenReturn(newMatch("sprint", true, false));
		when(liveMatchRepository.findByMatchName("standard")).thenReturn(newMatch("standard", true, false));
		doReturn(sprintMatch).when(localMatchService).getMatchCached("sprint");
		doReturn(standardMatch).when(localMatchService).getMatchCached("standard");

		var sprintView = controller.displayVorlaufMatch("sprint", new ExtendedModelMap());
		var standardView = controller.displayVorlaufMatch("standard", new ExtendedModelMap());

		assertAll("preliminary match views",
				() -> assertEquals("matchDisplay/match-display-vorlauf-sprint", sprintView,
						"Forty-throw games should use the sprint view"),
				() -> assertEquals("matchDisplay/match-display-vorlauf", standardView,
						"Other preliminary games should use the standard view")
		);
	}

	@Test
	void flatMatchEndpointReturnsNullWhenTheCachedMatchIsMissing() {
		when(localMatchService.getMatchCached("missing")).thenThrow(new IllegalArgumentException("missing"));

		var result = controller.match("missing");

		assertNull(result, "A missing cached match should produce a null response");
		verifyNoInteractions(matchFlattener);
	}

	@Test
	void flatMatchEndpointDelegatesToTheFlattener() {
		var match = mockMatch();
		@SuppressWarnings("unchecked")
		var flatMatch = (MatchFlat<Game>) mock(MatchFlat.class);
		doReturn(match).when(localMatchService).getMatchCached("event");
		doReturn(flatMatch).when(matchFlattener).flat(match);

		var result = controller.match("event");

		assertSame(flatMatch, result, "The flattener's result should be returned to the caller");
	}

	@Test
	void unimplementedHeadToHeadViewsReturnTheEmptyTemplate() {
		when(liveMatchRepository.findByMatchName("match")).thenReturn(newMatch("match", true, false));
		var model = new ExtendedModelMap();

		var fourPlayerView = controller.display4AgainstMatch("match", model);
		var twoPlayerView = controller.display2AgainstMatch("match", new ExtendedModelMap());

		assertAll("head-to-head views",
				() -> assertEquals("matchDisplay/empty", fourPlayerView,
						"The four-player view should use the current empty placeholder"),
				() -> assertEquals("matchDisplay/empty", twoPlayerView,
						"The two-player view should use the current empty placeholder"),
				() -> assertTrue(model.isEmpty(), "The four-player placeholder should not add unnecessary model data")
		);
	}

	private static LiveMatch newMatch(String name, boolean running, boolean isStatic) {
		return new LiveMatch(LocalDate.of(2026, 11, 7), name, name, MatchType.FINALE, running, isStatic, false);
	}

	@SuppressWarnings("unchecked")
	private static Match<Game> mockMatch() {
		return (Match<Game>) mock(Match.class);
	}
}
