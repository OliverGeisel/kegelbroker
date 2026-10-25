package de.olivergeisel.kegelbroker.client

import org.springframework.data.repository.CrudRepository
import org.springframework.data.util.Streamable
import java.time.LocalDate
import java.util.UUID

/**
 * Repository for the [LiveMatch] entity. It extends the CrudRepository interface and provides methods to find matches
 * by date, name, running state and match type.
 *
 * @author Oliver Geisel
 * @version 1.0
 * @since 1.0
 */
interface LiveMatchRepository : CrudRepository<LiveMatch, UUID>{

	override fun findAll(): Streamable<LiveMatch>

	fun findAllByDate(date : LocalDate): Streamable<LiveMatch>

	fun findByMatchName(matchName: String): LiveMatch?

	fun findByMatchNameIgnoreCase(matchName: String) : LiveMatch?

	fun existsByMatchName(matchName: String): Boolean

	fun findByRunning(running: Boolean): Streamable<LiveMatch>

	fun findByMatchType(matchType: MatchType): Streamable<LiveMatch>

	fun findByRunningAndStatic(running: Boolean, static: Boolean): Streamable<LiveMatch>

}