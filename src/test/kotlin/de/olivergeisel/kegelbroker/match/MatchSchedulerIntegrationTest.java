package de.olivergeisel.kegelbroker.match;

import de.olivergeisel.kegelbroker.client.LiveMatch;
import de.olivergeisel.kegelbroker.client.LiveMatchRepository;
import de.olivergeisel.kegelbroker.client.MatchType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
@Tag("repository")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MatchScheduler.class)
@Testcontainers(disabledWithoutDocker = true)
class MatchSchedulerIntegrationTest {

	@Container
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private LiveMatchRepository liveMatchRepository;

	@Autowired
	private MatchScheduler matchScheduler;

	@DynamicPropertySource
	static void configurePostgres(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
	}

	@Test
	void removesOldMatchesFromTheDatabaseAndKeepsRecentMatches() {
		var today = LocalDate.now();
		liveMatchRepository.save(new LiveMatch(today.minusDays(2), "old", "old", MatchType.FINALE, true, false, false));
		liveMatchRepository.save(new LiveMatch(today.minusDays(1), "yesterday", "yesterday", MatchType.FINALE, true, false, false));
		liveMatchRepository.save(new LiveMatch(today, "today", "today", MatchType.FINALE, true, false, false));

		matchScheduler.removeOldMatches();

		assertNull(liveMatchRepository.findByMatchName("old"));
		assertNotNull(liveMatchRepository.findByMatchName("yesterday"));
		assertNotNull(liveMatchRepository.findByMatchName("today"));
	}
}
