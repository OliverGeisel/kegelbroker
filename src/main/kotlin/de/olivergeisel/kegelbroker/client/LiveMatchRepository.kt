package de.olivergeisel.kegelbroker.client

import org.springframework.data.repository.CrudRepository
import org.springframework.data.util.Streamable
import java.util.UUID

interface LiveMatchRepository : CrudRepository<LiveMatch, UUID>{

	override fun findAll(): Streamable<LiveMatch>

	fun findByMatchName(matchName: String): LiveMatch?

	fun existsByMatchName(matchName: String): Boolean

	fun findByRunning(running: Boolean): Streamable<LiveMatch>

	fun findByMatchType(matchType: MatchType): Streamable<LiveMatch>

	fun findByRunningAndStatic(running: Boolean, static: Boolean): Streamable<LiveMatch>

}