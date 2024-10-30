package de.olivergeisel.kegelbroker.client

import jakarta.persistence.*
import java.time.LocalDate
import java.util.*
import kotlin.io.path.Path

/**
 * Entity to represent a Match, that is hosted on the application
 * It contains
 *
 * @author Oliver Geisel
 *
 * @since 1.0.0
 * @version 1.0.0
 */
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

	@ElementCollection
	var teams: List<String> = LinkedList<String>()

	private fun LiveMatch() {

	}

	fun getTeamsAsString(): String{
		return teams.joinToString(", ")
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

/**
 * Enum to represent the type of match
 * <ul>
 *     <li>FINALE</li>
 *     <li>HALBFINALE</li>
 *     <li>VORLAUF</li>
 *     <li>TEAMS2_4S_120</li>
 *     <li>TEAMS2_6S_120</li>
 * </ul>
 */
enum class MatchType {

	FINALE,
	HALBFINALE,
	VORLAUF,
	TEAMS2_4S_120,
	TEAMS2_6S_120,

}
