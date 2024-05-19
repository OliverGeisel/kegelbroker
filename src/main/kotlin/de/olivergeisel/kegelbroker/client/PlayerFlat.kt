package de.olivergeisel.kegelbroker.client

import core.game.Game
import core.team_and_player.Player

class PlayerFlat(player: Player<out Game>, private val gameFlat: GameFlat) : Player<GameFlat>(
	player.vorname,
	player.nachname,
	player.club,
	player.team,
	player.birthday
) {

	override fun getGame(): GameFlat {
		return gameFlat
	}

}
