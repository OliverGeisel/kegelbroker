package de.olivergeisel.kegelbroker

import org.springframework.stereotype.Service

@Service
class GameService( ) {

	private fun loadFromJson(): GameOverview {
		return GameOverview()
	}
	fun getGame(): GameOverview {
		return GameOverview()
	}
}