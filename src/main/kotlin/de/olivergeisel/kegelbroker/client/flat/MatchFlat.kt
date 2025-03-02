package de.olivergeisel.kegelbroker.client.flat

import com.fasterxml.jackson.annotation.JsonInclude
import core.game.Game
import core.match.Match
import core.team_and_player.Player
import core.team_and_player.Team
import core.util.Pair

@JsonInclude(JsonInclude.Include.NON_NULL)
class MatchFlat<G : Game>(private val iMatch: Match<G>, val teams: List<TeamFlat<G>>, val extra: Any) : Match<GameFlat>(
	iMatch.config,
	iMatch.generalMatchInfo,
	iMatch.statusInfo,
	null,
	null
) {

	var finished = iMatch.statusInfo.isFinished
	var aborted = iMatch.statusInfo.isAborted
	var final = iMatch.statusInfo.isFinished || iMatch.statusInfo.isAborted
	private var points: Map<String, Double> = iMatch.points
	private var setPoints: MutableMap<String, Double> = iMatch.setPoints

	override fun getTeams(): Array<out Team<GameFlat>> {
		return teams.toTypedArray()
	}

	override fun getGames(): MutableList<GameFlat> {
		return emptyList<GameFlat>().toMutableList()
	}

	override fun getPoints(): Map<String, Double> {
		return points
	}

	override fun getSetPoints(): MutableMap<String, Double> {
		return setPoints
	}

	override fun getCurrentPlayers(): MutableList<Player<GameFlat>> {
		val players = iMatch.getCurrentPlayers();
		val mapped = players.map { PlayerFlat(it, GameFlat(it.game)) }.toMutableList()
		return mapped as MutableList<Player<GameFlat>>
	}

	override fun getPlayerForSet(): MutableList<Pair<Int, Int>> {
		return emptyList<Pair<Int, Int>>().toMutableList()
	}

	override fun getGamesByPlayerPos(): MutableList<GameFlat> {
		return emptyList<GameFlat>().toMutableList()
	}

	override fun getGameRounds(): MutableList<MutableList<GameFlat>> {
		return iMatch.gameRounds.map { list -> list.map { GameFlat(it) }.toMutableList() }.toMutableList()
	}
}
