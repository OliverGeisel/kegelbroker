package de.olivergeisel.kegelbroker.client

import core.game.Game120
import core.match.Match
import core.point_system._2Teams120PointSystem
import de.olivergeisel.kegelbroker.ApplicationProperties
import de.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader
import de.kegelplay.infrastructure.update.MatchUpdater


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.util.Streamable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Formatter
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/**
 * Service for managing local matches
 * @param applicationProperties the application properties
 * @param liveMatchRepository the repository for live matches
 * @param cache the cache manager to store matches in
 */
@Service
class LocalMatchService(
	private val applicationProperties: ApplicationProperties,
	private val liveMatchRepository: LiveMatchRepository,
	private val cache: CacheManager
) {

	private object LOGGER {
		val logger: Logger = LoggerFactory.getLogger(LocalMatchService::class.java)
	}

	private fun loadDatePath(): Path {
		return Path.of(applicationProperties.dataPath)
	}

	fun getAllMatchesToday():Streamable<LiveMatch>{
		return liveMatchRepository.findAllByDate(LocalDate.now())
	}


	fun getMatchNames(day: LocalDate): List<String> {
		val dateFormatted = day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
		val dayDir = loadDatePath().resolve(dateFormatted)
		return dayDir.toFile().listFiles()?.map { it.name } ?: emptyList()
	}

	fun createMatch(form: MatchCreateForm) {
		val match = LiveMatch(form.matchDate, form.matchName, form.matchId, form.matchType)
		require(!liveMatchRepository.existsByMatchName(match.matchName)) {
			"Match with id ${form.matchName} already exists"
		}
		val teamNames = loadTeamNames(match)
		match.teams = teamNames
		cache.getCache("matches")?.put(match.matchName, MatchUpdater(getMatchFromDisk(form.matchDate, form.matchId)))
		liveMatchRepository.save(match)
	}

	private fun loadTeamNames(match: LiveMatch): List<String> {
		val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
		val path = Paths.get(applicationProperties.dataPath).resolve(match.date.format(formatter)).resolve(match.matchDir)
		val teamNamesDir = Files.list(path).filter{it.isDirectory() && it.name!= "Backup-Daten"}.map { it.name }
		return teamNamesDir.toList()
	}

	@Scheduled(fixedRate = 5_000)
	fun updateMatch() {
		for (match in liveMatchRepository.findByRunningAndStatic(running = true, static = false)) {
			val updater = cache.getCache("matches")?.get(match.matchName)?.let { it.get() as MatchUpdater<*> }
			val matchName = match.matchName
			val start = LocalDateTime.now()
			LOGGER.logger.info("Updating match '$matchName' at $start")
			try {
				updater?.updateMatch()
			} catch (e: Exception) {
				LOGGER.logger.warn("Failed to update match $matchName")
				continue
			}
			val end = LocalDateTime.now()
			LOGGER.logger.info("Match '$matchName' updated in ${Duration.between(start, end).toMillis()} ms")
			if (updater?.match?.statusInfo?.isFinished!! || updater.match.statusInfo.isAborted) {
				LOGGER.logger.info("Match '$matchName' finished")
				match.endMatch()
				liveMatchRepository.save(match)
			}
		}
	}

	fun getMatchCached(matchName: String): Match<*> {
		val updater: MatchUpdater<*>? = cache.getCache("matches")?.get(matchName)?.let { it.get() as MatchUpdater<*> }
		if (updater != null) {
			return updater.match
		}
		throw IllegalArgumentException("Match '$matchName' not found")
	}

	fun reloadMatch(matchName: String) {
		val match = liveMatchRepository.findByMatchName(matchName)
		if (match != null) {
			cache.getCache("matches")?.put(matchName, MatchUpdater(getMatchFromDisk(match.date, match.matchDir)))
		}
	}

	/**
	 * Load a match from disk
	 * @param date the date of the match
	 * @param matchName the name of the match
	 * @return the match object
	 */
	fun getMatchFromDisk(date: LocalDate, matchName: String): Match<Game120> {
		val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
		val dayDir = loadDatePath().resolve(dateFormatted)
		val matchDir = dayDir.resolve(matchName)
		val reader = KeglerheimGeneralReader(matchDir, true)
		val match: Match<Game120> = reader.initNewMatch()
		match.pointSystem = _2Teams120PointSystem()
		return match
	}

	/**
	 * Delete a match from the database. Removes the match from the cache as well.
	 * @param match the match to delete
	 */
	fun deleteMatch(match: LiveMatch) {
		try {
			val matchOp = liveMatchRepository.findById(match.id!!)
			liveMatchRepository.delete(matchOp.get())
			cache.getCache("matches")?.evict(matchOp.get().matchName)

		}catch (_ : java.lang.IllegalArgumentException){
			LOGGER.logger.warn("Match not found! '$match' was not deleted")
		}
	}


}
