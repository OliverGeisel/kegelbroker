package de.olivergeisel.kegelbroker.client.flat

import core.game.Game
import core.team_and_player.Player

/**
 * Flat version of [Player] for JSON serialization.
 * Converts a normal [Player] to a flat version for JSON serialization, which is used for the REST API.
 * That means no cyclic references and no complex objects, only primitive types and simple objects.
 *
 * @author Oliver Geisel
 * @since 1.0
 * @version 1.0
 */
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
