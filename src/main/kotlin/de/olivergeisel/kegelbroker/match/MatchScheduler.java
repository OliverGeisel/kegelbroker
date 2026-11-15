package de.olivergeisel.kegelbroker.match;


import de.olivergeisel.kegelbroker.client.LiveMatchRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class MatchScheduler {

	private final LiveMatchRepository liveMatchRepository;

	public MatchScheduler(LiveMatchRepository liveMatchRepository) {this.liveMatchRepository = liveMatchRepository;}

	@Scheduled(cron = "0 0 0 * * ?") // every 5 minutes
	void removeOldMatches() {
		var allMatches = liveMatchRepository.findAll();
		allMatches.stream()
				.filter(it -> it.getDate().isBefore(java.time.LocalDate.now(ZoneId.systemDefault()).minusDays(1)))
				.forEach(liveMatchRepository::delete);
	}
}
