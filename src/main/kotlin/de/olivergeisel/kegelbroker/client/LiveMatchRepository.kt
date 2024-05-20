package de.olivergeisel.kegelbroker.client

import org.springframework.data.repository.CrudRepository
import org.springframework.data.util.Streamable
import java.util.UUID

interface LiveMatchRepository : CrudRepository<LiveMatch, UUID>{

	override fun findAll(): Streamable<LiveMatch>

	fun findByMatchName(matchName: String): LiveMatch?

	fun existsByMatchName(matchName: String): Boolean

}