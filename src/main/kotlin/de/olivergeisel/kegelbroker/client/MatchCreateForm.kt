package de.olivergeisel.kegelbroker.client

import java.time.LocalDate

/**
 * Form to create a match. It contains the name, date, id, point system, url, local server and match type of the match.
 *
 * @author Oliver Geisel
 * @since 1.0
 * @version 1.0
 */
class MatchCreateForm (var matchName:String, var matchDate: LocalDate, var matchId:String, var pointSystem:String,
	var url:String="", var localServer :Boolean = false, var matchType: MatchType) {

}