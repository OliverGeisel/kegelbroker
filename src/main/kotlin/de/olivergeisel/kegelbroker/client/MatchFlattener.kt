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
		return MatchFlat(match, teams)
	}
}
