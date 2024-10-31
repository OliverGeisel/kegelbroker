package de.olivergeisel.kegelbroker.client.flat

import core.game.Game
import core.match.Match
import org.springframework.stereotype.Service
import java.util.*


/**
 * This class is used to flatten a {@link Match} for json representation.
 *
 * @author Oliver Geisel
 * @see Match
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
class MatchFlattener<T : Game> {

	// TODO change to builder or Factory
	/**
	 * Flattens a {@link Match} for json representation.
	 * @param match the match to flatten
	 * @return the flattened match
	 */
	fun <G : Game> flat(match: Match<G>): MatchFlat<G> {
		val matchStatusInfo = match.statusInfo
		// flat teams
		val teams = LinkedList<TeamFlat<G>>()
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
