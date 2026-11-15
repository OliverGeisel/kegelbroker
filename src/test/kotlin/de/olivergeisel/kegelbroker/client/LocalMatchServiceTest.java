package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.game.Game120;
import core.match.Match;
import de.kegelplay.infrastructure.data_reader.AlternatingGameKindGeneralReader;
import de.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader;
import de.kegelplay.infrastructure.update.MatchUpdater;
import de.kegelplay.infrastructure.update.SpecialMatchUpdater;
import de.olivergeisel.kegelbroker.ApplicationProperties;
import de.olivergeisel.kegelbroker.match_tree.MatchTreeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.util.Streamable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;

@Tag("unit")
@Tag("service")
class LocalMatchServiceTest {

	@TempDir
	Path tempDir;

	private ApplicationProperties applicationProperties;
	private LiveMatchRepository liveMatchRepository;
	private CacheManager cacheManager;
	private Cache matchCache;
	private MatchTreeRepository matchTreeRepository;
	private LocalMatchService localMatchService;

	@BeforeEach
	void setUp() {
		applicationProperties = new ApplicationProperties();
		ReflectionTestUtils.setField(applicationProperties, "dataPath", tempDir.toString());
		liveMatchRepository = mock(LiveMatchRepository.class);
		cacheManager = mock(CacheManager.class);
		matchCache = mock(Cache.class);
		matchTreeRepository = mock(MatchTreeRepository.class);
		localMatchService = new LocalMatchService(applicationProperties, liveMatchRepository, cacheManager,
				matchTreeRepository);
	}

	@Test
	void returnsAllMatchesForToday() {
		var matches = Streamable.of(newMatch("today"));
		when(liveMatchRepository.findAllByDate(LocalDate.now())).thenReturn(matches);

		var result = localMatchService.getAllMatchesToday();

		assertSame(matches, result, "The repository result for today should be returned unchanged");
		verify(liveMatchRepository).findAllByDate(LocalDate.now());
	}

	@Test
	void returnsAllMatchesForTheRequestedDay() {
		var day = LocalDate.of(2026, 11, 6);
		var matches = Streamable.of(newMatch("requested-day"));
		when(liveMatchRepository.findAllByDate(day)).thenReturn(matches);

		var result = localMatchService.getAllMatchesOf(day);

		assertSame(matches, result, "The repository result for the requested day should be returned unchanged");
		verify(liveMatchRepository).findAllByDate(day);
	}

	@Test
	void returnsDirectoryEntriesForTheFormattedDay() throws Exception {
		var day = LocalDate.of(2026, 11, 7);
		var dayDirectory = Files.createDirectories(tempDir.resolve(day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
		Files.createDirectory(dayDirectory.resolve("Match A"));
		Files.createFile(dayDirectory.resolve("notes.txt"));

		var names = localMatchService.getMatchNames(day);

		assertEquals(java.util.Set.of("Match A", "notes.txt"), java.util.Set.copyOf(names),
				"Match names should come from the requested date directory");
	}

	@Test
	void returnsAnEmptyListWhenTheDayDirectoryDoesNotExist() {
		var names = localMatchService.getMatchNames(LocalDate.of(2026, 11, 7));

		assertTrue(names.isEmpty(), "A missing day directory should produce no names");
	}

	@Test
	void mapsEachMatchDirectoryToItsTeamDirectories() throws Exception {
		var day = LocalDate.of(2026, 11, 7);
		var dayDirectory = Files.createDirectories(tempDir.resolve(day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
		var matchA = Files.createDirectory(dayDirectory.resolve("Match A"));
		var matchB = Files.createDirectory(dayDirectory.resolve("Match B"));
		Files.createDirectory(matchA.resolve("Team A"));
		Files.createDirectory(matchA.resolve("Team B"));
		Files.createDirectory(matchA.resolve("Backup-Daten"));
		Files.createFile(matchA.resolve("notes.txt"));
		Files.createDirectory(matchB.resolve("Team C"));
		Files.createFile(dayDirectory.resolve("not-a-match.txt"));

		var result = localMatchService.getMatchTeams(day);

		assertEquals(Map.of(
				"Match A", List.of("Team A", "Team B"),
				"Match B", List.of("Team C")
		), result, "The mapping should contain only match folders and their team folders");
	}

	@Test
	void returnsAnEmptyMappingWhenTheDayDirectoryDoesNotExist() {
		var result = localMatchService.getMatchTeams(LocalDate.of(2026, 11, 7));

		assertTrue(result.isEmpty(), "A missing day directory should produce no match/team entries");
	}

	@Test
	void rejectsDuplicateMatchNamesBeforeReadingFilesOrSaving() {
		var form = form("already-exists", "match-dir");
		when(liveMatchRepository.existsByMatchName("already-exists")).thenReturn(true);

		var exception = assertThrows(IllegalArgumentException.class, () -> localMatchService.createMatch(form),
				"A duplicate match name should be rejected");

		assertEquals("Match with id already-exists already exists", exception.getMessage(),
				"The duplicate error should identify the conflicting match");
		verify(liveMatchRepository, never()).save(any(LiveMatch.class));
		verifyNoInteractions(cacheManager);
	}

	@Test
	void rejectsDuplicateSpecialMatchNamesBeforeReadingFilesOrSaving() {
		var form = form("already-exists", "match-dir");
		when(liveMatchRepository.existsByMatchName("already-exists")).thenReturn(true);

		var exception = assertThrows(IllegalArgumentException.class,
				() -> localMatchService.createSpecialMatch(form), "A duplicate special match name should be rejected");

		assertEquals("Match with id already-exists already exists", exception.getMessage(),
				"The duplicate error should identify the conflicting special match");
		verify(liveMatchRepository, never()).save(any(LiveMatch.class));
		verifyNoInteractions(cacheManager);
	}

	@Test
	void createsMatchFromDiskAndPersistsTeamNamesAndUpdater() throws Exception {
		var form = form("new-match", "match-dir");
		var day = form.getMatchDate();
		var matchDirectory = createMatchDirectory(day, form.getMatchId());
		Files.createDirectory(matchDirectory.resolve("Team A"));
		Files.createDirectory(matchDirectory.resolve("Team B"));
		Files.createDirectory(matchDirectory.resolve("Backup-Daten"));
		Files.createFile(matchDirectory.resolve("notes.txt"));
		@SuppressWarnings("unchecked")
		Match<Game120> sourceMatch = (Match<Game120>) mock(Match.class);
		when(liveMatchRepository.existsByMatchName("new-match")).thenReturn(false);
		when(cacheManager.getCache("matches")).thenReturn(matchCache);

		try (var readers = mockConstruction(KeglerheimGeneralReader.class,
				(reader, context) -> doReturn(sourceMatch).when(reader).initNewMatch())) {
			localMatchService.createMatch(form);

			var savedMatch = captureSavedMatch();
			assertAll("created match",
					() -> assertEquals(Set.of("Team A", "Team B"), Set.copyOf(savedMatch.getTeams()),
							"Only team directories should be loaded; backup and file entries should be excluded"),
					() -> assertEquals("new-match", savedMatch.getMatchName(),
							"The created entity should use the form's match name"),
					() -> assertEquals(1, readers.constructed().size(),
							"Creating a standard match should load it from disk once"),
					() -> verify(matchCache).put(eq("new-match"), argThat(value -> value instanceof MatchUpdater<?>))
			);
		}
		verify(liveMatchRepository).save(any(LiveMatch.class));
	}

	@Test
	void createsSpecialMatchWithSpecialUpdater() throws Exception {
		var form = form("special-match", "special-dir");
		var matchDirectory = createMatchDirectory(form.getMatchDate(), form.getMatchId());
		Files.createDirectory(matchDirectory.resolve("Team A"));
		@SuppressWarnings("unchecked")
		Match<Game> sourceMatch = (Match<Game>) mock(Match.class);
		when(liveMatchRepository.existsByMatchName("special-match")).thenReturn(false);
		when(cacheManager.getCache("matches")).thenReturn(matchCache);

		try (var readers = mockConstruction(AlternatingGameKindGeneralReader.class,
				(reader, context) -> when(reader.initNewMatch()).thenReturn(sourceMatch))) {
			localMatchService.createSpecialMatch(form);

			var savedMatch = captureSavedMatch();
			assertAll("created special match",
					() -> assertEquals(List.of("Team A"), savedMatch.getTeams(),
							"Special match creation should retain the discovered team names"),
					() -> assertEquals(1, readers.constructed().size(),
							"Creating a special match should load it from disk once"),
					() -> verify(matchCache).put(eq("special-match"),
							argThat(value -> value instanceof SpecialMatchUpdater))
			);
		}
		verify(liveMatchRepository).save(any(LiveMatch.class));
	}

	@Test
	void reportsMissingMatchCacheEntry() {
		when(cacheManager.getCache("matches")).thenReturn(matchCache);
		when(matchCache.get("missing")).thenReturn(null);

		var exception = assertThrows(IllegalArgumentException.class, () -> localMatchService.getMatchCached("missing"),
				"A match absent from the cache should be reported as missing");

		assertEquals("Match 'missing' not found", exception.getMessage(),
				"The cache miss error should include the requested match name");
	}

	@Test
	void returnsMatchFromTheCacheWhenAnUpdaterIsPresent() {
		@SuppressWarnings("unchecked")
		var updater = (MatchUpdater<Game>) mock(MatchUpdater.class);
		@SuppressWarnings("unchecked")
		var cachedMatch = (Match<Game>) mock(Match.class);
		var valueWrapper = mock(Cache.ValueWrapper.class);
		doReturn(cachedMatch).when(updater).getMatch();
		when(cacheManager.getCache("matches")).thenReturn(matchCache);
		when(matchCache.get("cached")).thenReturn(valueWrapper);
		when(valueWrapper.get()).thenReturn(updater);

		var result = localMatchService.getMatchCached("cached");

		assertSame(cachedMatch, result, "The cached updater's match should be returned");
	}

	@Test
	void loadsStandardMatchFromDiskAndAssignsItsPointSystem() {
		var day = LocalDate.of(2026, 11, 7);
		@SuppressWarnings("unchecked")
		Match<Game120> sourceMatch = (Match<Game120>) mock(Match.class);

		try (var readers = mockConstruction(KeglerheimGeneralReader.class,
				(reader, context) -> doReturn(sourceMatch).when(reader).initNewMatch())) {
			var result = localMatchService.getMatchFromDisk(day, "match-dir");

			assertAll("loaded match",
					() -> assertSame(sourceMatch, result, "The reader's match should be returned"),
					() -> assertEquals(1, readers.constructed().size(), "One reader should be created for the match"),
					() -> verify(sourceMatch).setPointSystem(any(core.point_system._2Teams120PointSystem.class))
			);
		}
	}

	@Test
	void reloadDoesNothingWhenTheMatchIsNotInTheRepository() {
		when(liveMatchRepository.findByMatchName("missing")).thenReturn(null);

		localMatchService.reloadMatch("missing");

		assertAll("missing-match reload",
				() -> verify(liveMatchRepository).findByMatchName("missing"),
				() -> verifyNoInteractions(cacheManager)
		);
	}

	@Test
	void reloadsStandardMatchIntoCache() {
		var match = newMatch("reload-standard");
		when(liveMatchRepository.findByMatchName("reload-standard")).thenReturn(match);
		when(cacheManager.getCache("matches")).thenReturn(matchCache);
		@SuppressWarnings("unchecked")
		Match<Game120> sourceMatch = (Match<Game120>) mock(Match.class);

		try (var readers = mockConstruction(KeglerheimGeneralReader.class,
				(reader, context) -> doReturn(sourceMatch).when(reader).initNewMatch())) {
			localMatchService.reloadMatch("reload-standard");

			assertAll("standard reload",
					() -> assertEquals(1, readers.constructed().size(),
							"Reloading a standard match should read it from disk"),
					() -> verify(matchCache).put(eq("reload-standard"),
							argThat(value -> value instanceof MatchUpdater<?>))
			);
		}
	}

	@Test
	void reloadsSpecialMatchWithSpecialUpdater() {
		var match = newMatch("reload-special");
		match.setSpecial(true);
		when(liveMatchRepository.findByMatchName("reload-special")).thenReturn(match);
		when(cacheManager.getCache("matches")).thenReturn(matchCache);
		@SuppressWarnings("unchecked")
		Match<Game> sourceMatch = (Match<Game>) mock(Match.class);

		try (var readers = mockConstruction(AlternatingGameKindGeneralReader.class,
				(reader, context) -> when(reader.initNewMatch()).thenReturn(sourceMatch))) {
			localMatchService.reloadMatch("reload-special");

			assertAll("special reload",
					() -> assertEquals(1, readers.constructed().size(),
							"Reloading a special match should read it from disk"),
					() -> verify(matchCache).put(eq("reload-special"),
							argThat(value -> value instanceof SpecialMatchUpdater))
			);
		}
	}

	@Test
	void updateDoesNothingWhenThereAreNoRunningUpdatableMatches() {
		when(liveMatchRepository.findByRunningAndStatic(true, false)).thenReturn(Streamable.empty());

		localMatchService.updateMatch();

		assertAll("empty update run",
				() -> verify(liveMatchRepository).findByRunningAndStatic(true, false),
				() -> verifyNoInteractions(cacheManager, matchTreeRepository)
		);
	}

	@Test
	void updateSkipsRunningMatchesWithoutCachedUpdater() {
		var match = newMatch("not-cached");
		when(liveMatchRepository.findByRunningAndStatic(true, false)).thenReturn(Streamable.of(match));

		localMatchService.updateMatch();

		assertAll("match without cached updater",
				() -> verify(liveMatchRepository).findByRunningAndStatic(true, false),
				() -> verify(liveMatchRepository, never()).save(match),
				() -> verifyNoInteractions(matchTreeRepository)
		);
	}

	@Test
	void deletesPersistedMatchAndEvictsItsCachedValue() {
		var match = newMatch("to-delete");
		var id = UUID.randomUUID();
		match.setId(id);
		when(liveMatchRepository.findById(id)).thenReturn(Optional.of(match));
		when(cacheManager.getCache("matches")).thenReturn(matchCache);

		localMatchService.deleteMatch(match);

		assertAll("deleted match",
				() -> verify(liveMatchRepository).delete(match),
				() -> verify(matchCache).evict("to-delete")
		);
	}

	@Test
	void missingPersistedMatchIsIgnoredDuringDelete() {
		var match = newMatch("not-persisted");
		var id = UUID.randomUUID();
		match.setId(id);
		when(liveMatchRepository.findById(id)).thenReturn(Optional.empty());

		localMatchService.deleteMatch(match);

		assertAll("missing match delete",
				() -> verify(liveMatchRepository).findById(id),
				() -> verify(liveMatchRepository, never()).delete(any(LiveMatch.class)),
				() -> verifyNoInteractions(cacheManager)
		);
	}

	@Test
	void matchWithoutIdIsIgnoredDuringDelete() {
		var match = newMatch("without-id");

		localMatchService.deleteMatch(match);

		assertAll("match without id",
				() -> verifyNoInteractions(liveMatchRepository),
				() -> verifyNoInteractions(cacheManager)
		);
	}

	private static LiveMatch newMatch(String name) {
		return new LiveMatch(LocalDate.of(2026, 11, 7), name, name, MatchType.FINALE, true, false, false);
	}

	private static MatchCreateForm form(String name, String matchId) {
		return new MatchCreateForm(name, LocalDate.of(2026, 11, 7), matchId, "120", "", false, MatchType.FINALE);
	}

	private Path createMatchDirectory(LocalDate day, String matchId) throws Exception {
		var dayDirectory = tempDir.resolve(day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
		return Files.createDirectories(dayDirectory.resolve(matchId));
	}

	private LiveMatch captureSavedMatch() {
		var captor = org.mockito.ArgumentCaptor.forClass(LiveMatch.class);
		verify(liveMatchRepository).save(captor.capture());
		return captor.getValue();
	}
}
