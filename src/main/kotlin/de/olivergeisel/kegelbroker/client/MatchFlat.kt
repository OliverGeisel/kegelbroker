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
	override fun getTeams(): Array<out Team<GameFlat>> {
		return teams.toTypedArray()
	}

	override fun getGames(): MutableList<GameFlat> {
		return emptyList<GameFlat>().toMutableList()
	}

	override fun getCurrentPlayers(): MutableList<Player<GameFlat>> {
		var players =  super.getCurrentPlayers();
		players.forEach{it.game=null}
		return players
	}

}
