package de.olivergeisel.kegelbroker.client

import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
class LiveMatch (var date: LocalDate, var matchName:String, var matchDir:String, var matchType:MatchType) {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	open var id: UUID? = null



	private fun LiveMatch() {

	}

	fun initMatch(){

	}

}

enum class MatchType {

	FINALE,
	HALBFINALE,
	VORLAUF,
	TEAMS2

}
