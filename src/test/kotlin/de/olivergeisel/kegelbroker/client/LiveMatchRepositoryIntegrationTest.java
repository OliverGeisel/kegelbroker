package de.olivergeisel.kegelbroker.client;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@Tag("repository")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class LiveMatchRepositoryIntegrationTest {

	@Container
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private LiveMatchRepository liveMatchRepository;

	@DynamicPropertySource
	static void configurePostgres(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
	}

	@Test
	void persistsMatchesAndFindsThemByDate() {
		var day = LocalDate.of(2026, 11, 7);
		liveMatchRepository.save(newMatch(day, "match-one", MatchType.FINALE, true, false));
		liveMatchRepository.save(newMatch(day, "match-two", MatchType.VORLAUF, false, false));
		liveMatchRepository.save(newMatch(day.plusDays(1), "tomorrow", MatchType.FINALE, true, false));

		var matchesOnDay = liveMatchRepository.findAllByDate(day).stream()
				.map(LiveMatch::getMatchName)
				.collect(java.util.stream.Collectors.toSet());

		assertEquals(Set.of("match-one", "match-two"), matchesOnDay,
				"Date lookup should return only matches scheduled for that date");
	}

	@Test
	void findsMatchesByExactAndCaseInsensitiveName() {
		var match = newMatch(LocalDate.of(2026, 11, 7), "City Cup", MatchType.FINALE, true, false);
		liveMatchRepository.save(match);

		assertAll("name queries",
				() -> assertNotNull(liveMatchRepository.findByMatchName("City Cup"),
						"Exact name lookup should find the persisted match"),
				() -> assertNull(liveMatchRepository.findByMatchName("city cup"),
						"Exact name lookup should remain case-sensitive"),
				() -> assertNotNull(liveMatchRepository.findByMatchNameIgnoreCase("cItY cUp"),
						"Case-insensitive lookup should find the persisted match"),
				() -> assertNull(liveMatchRepository.findByMatchNameIgnoreCase("missing"),
						"A missing name should return no match"),
				() -> assertTrue(liveMatchRepository.existsByMatchName("City Cup"),
						"Existence lookup should return true for a persisted name"),
				() -> assertFalse(liveMatchRepository.existsByMatchName("Missing"),
						"Existence lookup should return false for an unknown name")
		);
	}

	@Test
	void filtersMatchesByRunningStateStaticStateAndMatchType() {
		var day = LocalDate.of(2026, 11, 7);
		liveMatchRepository.save(newMatch(day, "running", MatchType.FINALE, true, false));
		liveMatchRepository.save(newMatch(day, "static", MatchType.FINALE, true, true));
		liveMatchRepository.save(newMatch(day, "finished", MatchType.FINALE, false, false));
		liveMatchRepository.save(newMatch(day, "preliminary", MatchType.VORLAUF, true, false));

		var runningNames = names(liveMatchRepository.findByRunning(true));
		var finishedNames = names(liveMatchRepository.findByRunning(false));
		var finaleNames = names(liveMatchRepository.findByMatchType(MatchType.FINALE));
		var runningAndUpdatingNames = names(liveMatchRepository.findByRunningAndStatic(true, false));
		var runningAndStaticNames = names(liveMatchRepository.findByRunningAndStatic(true, true));

		assertAll("state and type queries",
				() -> assertEquals(Set.of("running", "static", "preliminary"), runningNames,
						"Running lookup should return every running match"),
				() -> assertEquals(Set.of("finished"), finishedNames,
						"Finished lookup should return only non-running matches"),
				() -> assertEquals(Set.of("running", "static", "finished"), finaleNames,
						"Match-type lookup should return all matches of the requested type"),
				() -> assertEquals(Set.of("running", "preliminary"), runningAndUpdatingNames,
						"Combined lookup should exclude static matches"),
				() -> assertEquals(Set.of("static"), runningAndStaticNames,
						"Combined lookup should find running static matches")
		);
	}

	private static LiveMatch newMatch(LocalDate date, String name, MatchType type, boolean running, boolean isStatic) {
		return new LiveMatch(date, name, name, type, running, isStatic, false);
	}

	private static Set<String> names(org.springframework.data.util.Streamable<LiveMatch> matches) {
		return matches.stream().map(LiveMatch::getMatchName).collect(java.util.stream.Collectors.toSet());
	}
}
