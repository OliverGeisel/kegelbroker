package de.olivergeisel.kegelbroker.match;

import de.olivergeisel.kegelbroker.client.LiveMatch;
import de.olivergeisel.kegelbroker.client.LiveMatchRepository;
import de.olivergeisel.kegelbroker.client.MatchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Streamable;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MatchSchedulerTest {

	private MatchScheduler matchScheduler;

	@Mock
	private LiveMatchRepository liveMatchRepository;

	@BeforeEach
	void setUp() {
		matchScheduler = new MatchScheduler(liveMatchRepository);
	}

	@Test
	void removesMatchesOlderThanOneDay() {
		var today = LocalDate.now();
		var oldMatch = new LiveMatch(today.minusDays(2), "old", "old", MatchType.FINALE, true, false, false);
		var yesterdayMatch = new LiveMatch(today.minusDays(1), "yesterday", "yesterday", MatchType.FINALE, true, false, false);
		var todayMatch = new LiveMatch(today, "today", "today", MatchType.FINALE, true, false, false);
		when(liveMatchRepository.findAll()).thenReturn(Streamable.of(oldMatch, yesterdayMatch, todayMatch));

		matchScheduler.removeOldMatches();

		verify(liveMatchRepository).findAll();
		verify(liveMatchRepository).delete(oldMatch);
		verify(liveMatchRepository, never()).delete(yesterdayMatch);
		verify(liveMatchRepository, never()).delete(todayMatch);
	}

	@Test
	void doesNotDeleteAnythingWhenThereAreNoMatches() {
		when(liveMatchRepository.findAll()).thenReturn(Streamable.empty());

		matchScheduler.removeOldMatches();

		verify(liveMatchRepository).findAll();
		verify(liveMatchRepository, never()).delete(any(LiveMatch.class));
	}
}