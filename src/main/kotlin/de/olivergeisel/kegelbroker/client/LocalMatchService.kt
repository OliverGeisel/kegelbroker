package de.olivergeisel.kegelbroker.client

import core.game.Game120
import core.match.Match
import core.point_system._2Teams120PointSystem
import de.olivergeisel.kegelbroker.ApplicationProperties
import de.olivergeisel.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader
import de.olivergeisel.kegelplay.infrastructure.update.MatchUpdater
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class LocalMatchService(private val applicationProperties: ApplicationProperties,
	private val liveMatchRepository: LiveMatchRepository,
	private val cache: CacheManager){

	private object LOGGER {
		val logger: Logger = LoggerFactory.getLogger(LocalMatchService::class.java)

	}
	private fun loadDatePath(): Path {
		return Path.of(applicationProperties.dataPath)
	}


	fun getMatchNames(day: LocalDate): List<String> {
		val dateFormatted = day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
		val dayDir = loadDatePath().resolve(dateFormatted)
		return dayDir.toFile().listFiles()?.map { it.name } ?: emptyList()
	}

	fun createMatch(form: MatchCreateForm) {
		val match = LiveMatch(form.matchDate, form.matchName, form.matchId, form.matchType)
		require (!liveMatchRepository.existsByMatchName(match.matchName)) {
			"Match with id ${form.matchName} already exists"
		}
		cache.getCache("matches")?.put(match.matchName, MatchUpdater(getMatchFromDisk(form.matchDate, form.matchId)))
		liveMatchRepository.save(match)
	}

	@Scheduled(fixedRate = 5_000)
	fun updateMatch() {
		for (match in liveMatchRepository.findByRunningAndStatic(running = true, static = false)) {
			val updater = cache.getCache("matches")?.get(match.matchName)?.let { it.get() as MatchUpdater<*> }
			val matchName = match.matchName
			val start = LocalDateTime.now()
			LOGGER.logger.info("Updating match '$matchName' at $start" )
			try {
				updater?.updateMatch()
			}catch (e: Exception) {
				LOGGER.logger.warn("Failed to update match $matchName")
				continue
			}
			val end = LocalDateTime.now()
			LOGGER.logger.info("Match $matchName updated in ${Duration.between(start, end).toMillis()} ms")
		}
	}

	fun getMatchCached(matchName: String): Match<*> {
		val updater: MatchUpdater<*>? = cache.getCache("matches")?.get(matchName)?.let { it.get() as MatchUpdater<*> }
		if (updater != null) {
			return updater.match
		}
		throw IllegalArgumentException("Match $matchName not found")
	}

	fun getMatchFromDisk(date: LocalDate, matchName: String): Match<Game120> {
		val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
		val dayDir = loadDatePath().resolve(dateFormatted)
		val matchDir = dayDir.resolve(matchName)
		 val reader = KeglerheimGeneralReader(matchDir,true)
		val match: Match<Game120> = reader.initNewMatch()
		match.pointSystem = _2Teams120PointSystem()
		return match
	}

	fun deleteMatch(match: LiveMatch) {
		val matchOp = liveMatchRepository.findByMatchName(match.matchName)
		if (matchOp != null) {
			liveMatchRepository.delete(matchOp)
			cache.getCache("matches")?.evict(matchOp.matchName)
		}
	}


}
