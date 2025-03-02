package de.olivergeisel.kegelbroker.client.flat

import com.fasterxml.jackson.annotation.JsonInclude
import core.game.Game
import core.team_and_player.GeneralTeamInfo
import core.team_and_player.Player
import core.team_and_player.Team

@JsonInclude(JsonInclude.Include.NON_NULL)
class TeamFlat<T : Game>(team: Team<T>) : Team<GameFlat>(team.name, team.generalTeamInfo, null, emptyArray()) {
	private var name: String = team.name
	private var generalTeamInfo = team.generalTeamInfo
	private var players = team.players.map { PlayerFlat(it,GameFlat(it.game)) }

	init {
		setPlayers(players.toTypedArray())
	}

	override fun getPlayers(): Array<out Player<GameFlat>> {
		return players.toTypedArray()
	}

	override fun getGeneralTeamInfo(): GeneralTeamInfo {
		return generalTeamInfo
	}

	override fun getName(): String {
		return name
	}

	override fun setName(p0: String?) {
		name = p0!!
	}

}
