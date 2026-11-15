package de.olivergeisel.kegelbroker.client


import core.game.Game
import core.game.Game120
import core.match.Match
import core.point_system._2Teams120PointSystem
import de.kegelplay.infrastructure.data_reader.AlternatingGameKindGeneralReader
import de.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader
import de.kegelplay.infrastructure.update.MatchUpdater
import de.kegelplay.infrastructure.update.SpecialMatchUpdater
import de.olivergeisel.kegelbroker.ApplicationProperties
import de.olivergeisel.kegelbroker.match_tree.MatchNode
import de.olivergeisel.kegelbroker.match_tree.MatchTreeRepository
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.util.Streamable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private const val DATE_FORMAT = "dd.MM.yyyy"

private const val CACHE_DOMAIN = "matches"

/**
 * Service for managing local matches. A local Match is only the data for a Match. The Source of these Matches are on
 * the Drive or got via an extra Service
 *
 * @param applicationProperties the application properties
 * @param liveMatchRepository the repository for live matches
 * @param cache the cache manager to store matches in
 * @param matchTreeRepository the repository for match trees
 */
@Service
@Transactional
class LocalMatchService(
	private val applicationProperties: ApplicationProperties,
	private val liveMatchRepository: LiveMatchRepository,
	private val cache: CacheManager,
	private val matchTreeRepository: MatchTreeRepository
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

	fun getAllMatchesOf(day: LocalDate): Streamable<LiveMatch> {
		return liveMatchRepository.findAllByDate(day)
	}


	fun getMatchNames(day: LocalDate): List<String> {
		val dateFormatted = day.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
		val dayDir = loadDatePath().resolve(dateFormatted)
		return dayDir.toFile().listFiles()?.map { it.name } ?: emptyList()
	}

	/**
	 * Get only the team names of Matches for the given date.
	 *
	 *
	 */
	fun getMatchTeams(day: LocalDate): Map<String, List<String>> {
		val dayDirectory = loadDatePath()
			.resolve(day.format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
		if (!Files.isDirectory(dayDirectory)) {
			return emptyMap()
		}

		return Files.list(dayDirectory).use { matchDirectories ->
			matchDirectories
				.filter { it.isDirectory() && it.name != "Backup-Daten" }
				.sorted()
				.toList()
				.associate { matchDirectory ->
					matchDirectory.name to readTeamNames(matchDirectory)
				}
		}
	}

	fun createMatch(form: MatchCreateForm) {
		val match = LiveMatch(form.matchDate, form.matchName, form.matchId, form.matchType)
		require(!liveMatchRepository.existsByMatchName(match.matchName)) {
			"Match with id ${form.matchName} already exists"
		}
		val teamNames = loadTeamNames(match)
		match.teams = teamNames
		cache.getCache(CACHE_DOMAIN)?.put(match.matchName, MatchUpdater(getMatchFromDisk(form.matchDate, form.matchId)))
		liveMatchRepository.save(match)
	}

	fun createSpecialMatch(form: MatchCreateForm) {
		val match = LiveMatch(form.matchDate, form.matchName, form.matchId, form.matchType)
		match.special = true
		require(!liveMatchRepository.existsByMatchName(match.matchName)) {
			"Match with id ${form.matchName} already exists"
		}
		val teamNames = loadTeamNames(match)
		match.teams = teamNames
		cache.getCache(CACHE_DOMAIN)?.put(
			match.matchName, SpecialMatchUpdater(
				getMatchFromDiskSpecial(
					form.matchDate, form
						.matchId
				)
			)
		)
		liveMatchRepository.save(match)
	}

	fun updateMatch(
		matchId: UUID,
		matchName: String,
		date: LocalDate,
		matchDir: String,
		matchType: MatchType,
		running: Boolean,
		static: Boolean,
		special: Boolean
	) {
		val match = liveMatchRepository.findById(matchId).orElseThrow()
		val updatedName = matchName.trim()
		require(updatedName.isNotEmpty()) { "Match name must not be empty" }
		require(
			updatedName.equals(match.matchName, ignoreCase = true) ||
				!liveMatchRepository.existsByMatchNameIgnoreCase(updatedName)
		) {
			"Match '$updatedName' already exists"
		}

		val sourceChanged = match.date != date || match.matchDir != matchDir || match.special != special
		val oldName = match.matchName
		val cachedUpdater = cache.getCache(CACHE_DOMAIN)?.get(oldName)?.get()
		val newUpdater: Any?
		val teams: List<String>
		if (sourceChanged) {
			val sourcePath = resolveMatchDirectory(date, matchDir)
			newUpdater = if (special) {
				SpecialMatchUpdater(AlternatingGameKindGeneralReader(sourcePath).initNewMatch())
			} else {
				MatchUpdater(getMatchFromDisk(date, matchDir))
			}
			teams = loadTeamNames(LiveMatch(date, updatedName, matchDir, matchType))
		} else {
			newUpdater = null
			teams = match.teams
		}

		match.matchName = updatedName
		match.date = date
		match.matchDir = matchDir
		match.matchType = matchType
		match.running = running
		match.static = static
		match.special = special
		match.teams = teams
		liveMatchRepository.save(match)

		if (newUpdater != null) {
			cache.getCache(CACHE_DOMAIN)?.put(updatedName, newUpdater)
		} else if (oldName != updatedName && cachedUpdater != null) {
			cache.getCache(CACHE_DOMAIN)?.put(updatedName, cachedUpdater)
		}
		if (oldName != updatedName) {
			cache.getCache(CACHE_DOMAIN)?.evict(oldName)
		}
	}

	private fun resolveMatchDirectory(date: LocalDate, matchDir: String): Path {
		val directoryName = Path.of(matchDir)
		require(!directoryName.isAbsolute && directoryName.nameCount == 1) {
			"Match directory must be a directory name"
		}
		val dayDirectory = loadDatePath()
			.resolve(date.format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
			.normalize()
		val matchDirectory = dayDirectory.resolve(directoryName).normalize()
		require(matchDirectory.parent == dayDirectory && Files.isDirectory(matchDirectory)) {
			"Match directory '$matchDir' does not exist for $date"
		}
		return matchDirectory
	}

	private fun getMatchFromDiskSpecial(date: LocalDate, matchName: String): Match<Game> {
		val dateFormatted = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
		val dayDir = loadDatePath().resolve(dateFormatted)
		val matchDir = dayDir.resolve(matchName)
		val reader = AlternatingGameKindGeneralReader(matchDir)
		val match = reader.initNewMatch()
		return match
	}

	private fun loadTeamNames(match: LiveMatch): List<String> {
		val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
		val path = loadDatePath().resolve(match.date.format(formatter)).resolve(match.matchDir)
		return readTeamNames(path)
	}

	private fun readTeamNames(matchDirectory: Path): List<String> {
		return Files.list(matchDirectory).use { teamDirectories ->
			teamDirectories
				.filter { it.isDirectory() && it.name != "Backup-Daten" }
				.sorted()
				.map { it.name }
				.toList()
		}
	}

	private fun updateMatchTree(match: LiveMatch, matchUpdater: MatchUpdater<*>) {
		val matchTrees = match.matchTree
		val realMatch = matchUpdater.match
		for (matchTree in matchTrees) {
			// root node
			val rootNode = matchTree.root
			// TODO assumes that there is only one team in the tree
			val teamNumber = rootNode.players[0].teamNumber
			val team = realMatch.teams[teamNumber]
			for (player in rootNode.players) {
				val teamIndex = player.teamIndex
				val realPlayer = team.players[teamIndex]
				player.name = realPlayer.completeName
				player.score = realPlayer.game.totalScore
			}
			for (child in rootNode.children) {
				updateChild(child, realMatch, teamNumber)
			}
			matchTreeRepository.save(matchTree)
		}
	}

	private fun updateChild(child: MatchNode?, realMatch: Match<out Game>, teamNumber: Int) {
		if (child == null) return
		val team = realMatch.teams[teamNumber]
		for (player in child.players) {
			val teamIndex = player.teamIndex
			val realPlayer = team.players[teamIndex]
			player.name = realPlayer.completeName
			player.score = realPlayer.game.totalScore
		}
		for (child in child.children) {
			updateChild(child, realMatch, teamNumber)
		}

	}

	@Scheduled(fixedRate = 5_000)
	fun updateMatch() {
		for (match in liveMatchRepository.findByRunningAndStatic(running = true, static = false)) {
			val updater = cache.getCache(CACHE_DOMAIN)?.get(match.matchName)?.let { it.get() as MatchUpdater<*> }
			val matchName = match.matchName
			val start = LocalDateTime.now()
			LOGGER.logger.info("Updating match '$matchName' at $start")
			try {
				updater?.updateMatch()
				updateMatchTree(match, updater!!)
			} catch (e: Exception) {
				LOGGER.logger.warn("Failed to update match $matchName")
				LOGGER.logger.debug("", e)
				continue
			}
			val end = LocalDateTime.now()
			LOGGER.logger.info("Match '$matchName' updated in ${Duration.between(start, end).toMillis()} ms")
			// Todo check when is it aborted
			if (updater.match?.statusInfo?.isFinished!!) {
				LOGGER.logger.info("Match '$matchName' finished")
				match.endMatch()
				liveMatchRepository.save(match)
			}
		}
	}

	/**
	 * Get a match from the cache. If the match is not in the cache, it will be loaded from disk and added to the cache.
	 * @param matchName the name of the match
	 * @return the match object
	 * @throws IllegalArgumentException if the match is not found
	 */
	fun getMatchCached(matchName: String): Match<*> {
		val updater: MatchUpdater<*>? = cache.getCache(CACHE_DOMAIN)?.get(matchName)?.let { it.get() as MatchUpdater<*> }
		if (updater != null) {
			return updater.match
		}
		throw IllegalArgumentException("Match '$matchName' not found")
	}

	/**
	 * Force reload a Match from disk and update the cache. This is useful if the match has been updated on disk and the
	 * cache needs to be refreshed.
	 */
	fun reloadMatch(matchName: String) {
		val match = liveMatchRepository.findByMatchName(matchName) ?: return
		if (match.special) {
			cache.getCache(CACHE_DOMAIN)
				?.put(matchName, SpecialMatchUpdater(getMatchFromDiskSpecial(match.date, match.matchDir)))
		} else {
			cache.getCache(CACHE_DOMAIN)
				?.put(matchName, MatchUpdater(getMatchFromDisk(match.date, match.matchDir)))
		}
	}

	/**
	 * Load a match from disk
	 * @param date the date of the match
	 * @param matchName the name of the match
	 * @return the match object
	 */
	fun getMatchFromDisk(date: LocalDate, matchName: String): Match<Game120> {
		val dateFormatted = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
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
		val matchId = match.id
		if (matchId == null) {
			LOGGER.logger.warn("Match not found! '$match' was not deleted")
			return
		}
		val persistedMatch = liveMatchRepository.findById(matchId).orElse(null)
		if (persistedMatch == null) {
			LOGGER.logger.warn("Match not found! '$match' was not deleted")
			return
		}
		liveMatchRepository.delete(persistedMatch)
		cache.getCache(CACHE_DOMAIN)?.evict(persistedMatch.matchName)
	}


}
