package de.olivergeisel.kegelbroker.client

import core.game.Game
import core.match.Match
import org.springframework.stereotype.Service
import java.util.*


/**
 * This class is used to flatten a {@link Match} for json representation.
 */
@Service
class MatchFlattener<T : Game> {

	/**
	 * Flattens a {@link Match} for json representation.
	 * @param match the match to flatten
	 * @return the flattened match
	 */
	fun flat(match: Match<T>): MatchFlat <T>{
		val matchStatusInfo = match.statusInfo
		// flat teams
		val teams = LinkedList<TeamFlat<T>>()
		for (team in match.teams) {
			val flatTeam = TeamFlat(team)
			teams.add(flatTeam)
		}
		val extra = LinkedHashMap<String, List<String>>()
		for (team in match.teams) {
			val teamName = team.name
			val playerNames = LinkedList<String>()
			val copy = team.players.toMutableList()
			copy.sortBy { it.game.totalScore }
			copy.reverse()
			copy.map { it.completeName }.forEach { playerNames.add(it) }
			extra[teamName] = playerNames
		}
		return MatchFlat(match, teams, extra)
	}
}
