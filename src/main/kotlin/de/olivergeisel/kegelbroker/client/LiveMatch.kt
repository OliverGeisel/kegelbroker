package de.olivergeisel.kegelbroker.client

import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
class LiveMatch(
	/**
	 * Date of the match
	 */
	var date: LocalDate,
	/**
	 * Name of the match
	 */
	var matchName: String,
	/**
	 * Directory of the match local
	 */
	var matchDir: String,
	/**
	 * Type of the match like finale, halbfinale, vorlauf
	 */
	val matchType: MatchType,
	/**
	 * Running state of the match.
	 * If false the match is finished
	 */
	var running: Boolean = true,
	/**
	 * Static state of the match. It will not change its state anymore
	 */
	var static :Boolean = false
) {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	open var id: UUID? = null

	private fun LiveMatch() {

	}

	fun initMatch() {

	}

	fun endMatch() {
		running = false
	}

	fun setStatic() {
		static = true
	}

	fun setRunning() {
		running = true
	}

	fun setUpdating(){
		static = false
	}

}

enum class MatchType {

	FINALE,
	HALBFINALE,
	VORLAUF,
	TEAMS2

}
