package de.olivergeisel.kegelbroker.client

import core.game.Game
import core.match.Match
import core.team_and_player.Player
import core.team_and_player.Team

class MatchFlat<G : Game>(match: Match<G>, val teams: List<TeamFlat<G>>, val extra: Any) : Match<GameFlat>(
	match.config,
	match.generalMatchInfo,
	match.statusInfo,
	null,
	null
) {

	var finished = match.statusInfo.isFinished
	var final = match.statusInfo.isFinished || match.statusInfo.isAborted
	private var points: Map<String, Double> = match.points

	override fun getTeams(): Array<out Team<GameFlat>> {
		return teams.toTypedArray()
	}

	override fun getGames(): MutableList<GameFlat> {
		return emptyList<GameFlat>().toMutableList()
	}

	override fun getPoints(): Map<String, Double> {
		return points
	}

	override fun getSetPoints(): Map<String?, Double?>? {
		TODO("Not yet implemented")
	}

	override fun getCurrentPlayers(): MutableList<Player<GameFlat>> {
		val players = super.getCurrentPlayers();
		players.forEach{it.game=null}
		return players
	}


}
