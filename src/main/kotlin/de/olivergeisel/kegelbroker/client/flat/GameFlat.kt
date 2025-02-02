package de.olivergeisel.kegelbroker.client.flat

import core.game.Game
import core.game.GameInfo
import core.game.GameKind
import core.game.GameSet
import core.team_and_player.Player
import java.time.LocalDateTime

class GameFlat(game: Game) : Game(null) {

	private var date = game.date
	private var playerName = game.currentPlayer.completeName
	private var sets = game.gameSets
	var info = game.gameInfo
	var kind = game.gameKind

	override fun getDate(): LocalDateTime {
		return date
	}

	override fun setDate(date: LocalDateTime?) {
		this.date = date
	}

	/**
	 * Returns the number of [GameSet]s of the game.
	 *
	 * @return Number of [GameSet]s
	 */
	override fun getNumberOfGameSets(): Int {
		return sets.size
	}

	override fun getPlayer(): Player<Game>? {
		return null
	}

	override fun setPlayer(p0: Player<Game>?) {
		if (p0 != null) {
			playerName = p0.completeName
		}
	}


	override fun start() {
		TODO("Not yet implemented")
	}

	override fun getGameSet(gameSetNumber: Int): GameSet {
		return sets[gameSetNumber]
	}

	override fun getGameInfo(): GameInfo {
		return info
	}

	override fun getGameKind(): GameKind {
		return kind
	}

	override fun getNumberOfWurf(): Int {
		return sets.sumOf { it.anzahlGespielteWuerfe }
	}

	override fun getTotalFehlwurf(): Int {
		return sets.sumOf { it.anzahlFehler }
	}

	override fun getTotalScore(): Int {
		return sets.sumOf { it.score }
	}

	override fun getTotalVolle(): Int {
		return sets.sumOf { it.volleScore }
	}

	override fun getTotalAbraeumen(): Int {
		return sets.sumOf { it.abraeumenScore }
	}

	override fun getGameSets(): Array<GameSet> {
		return sets
	}

	override fun setGameSets(gameSets: MutableList<GameSet>?) {
		sets = gameSets!!.toTypedArray()
	}
}
