package de.olivergeisel.kegelbroker.game

import org.springframework.stereotype.Service

/**
 * A Service to load GameOverviews from some source.
 *
 * @author Oliver Geisel
 * @since 1.0
 * @version 1.0
 */
@Service
class GameService( ) {

	private fun loadFromJson(): GameOverview {
		return GameOverview()
	}
	fun getGame(): GameOverview {
		return GameOverview()
	}
}