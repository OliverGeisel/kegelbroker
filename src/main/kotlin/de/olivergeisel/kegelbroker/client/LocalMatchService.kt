package de.olivergeisel.kegelbroker.client

import core.game.Game
import core.game.Game120
import core.match.Match
import core.point_system._2Teams120PointSystem
import de.olivergeisel.kegelbroker.ApplicationProperties
import de.olivergeisel.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class LocalMatchService(private val applicationProperties: ApplicationProperties,
	private val liveMatchRepository: LiveMatchRepository){


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
		liveMatchRepository.save(match)
	}

	fun getMatch(date: LocalDate, matchName: String): Match<Game120> {
		val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
		val dayDir = loadDatePath().resolve(dateFormatted)
		val matchDir = dayDir.resolve(matchName)
		 val reader = KeglerheimGeneralReader(matchDir,true)
		val match: Match<Game120> = reader.initNewMatch()
		match.pointSystem = _2Teams120PointSystem()
		return match
	}


}
