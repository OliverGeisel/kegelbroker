package de.olivergeisel.kegelbroker.client.flat

import com.fasterxml.jackson.annotation.JsonInclude
import core.game.Game
import core.team_and_player.GeneralTeamInfo
import core.team_and_player.Player
import core.team_and_player.Team

/**
 * Flat version of [Team] for JSON serialization.
 * Converts a normal [Team] to a flat version for JSON serialization, which is used for the REST API.
 * That means no cyclic references and no complex objects, only primitive types and simple objects.
 *
 * @author Oliver Geisel
 * @since 1.0
 * @version 1.0
 */
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
