package de.olivergeisel.kegelbroker.client

import core.game.Game
import core.game.Game120
import core.match.Match
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate

@Controller
@PreAuthorize("hasRole('ADMIN')")
class LandingController(
	private val localMatchService: LocalMatchService,
	private val matchFlattener: MatchFlattener<Game120>,
	private val matchRepository: LiveMatchRepository
) {

	private val LOGGER = LoggerFactory.getLogger(LandingController::class.java)

	@GetMapping("/")
	fun landing(model: Model): String {
		model.addAttribute("matches", matchRepository.findAll())
		return "landing"
	}

	@GetMapping("/create")
	fun createMatch(model: Model): String {
		model.addAttribute("pointSystems", listOf("2 Teams", "Vorlauf", "Paarweise", "4 gegeneinander"))
		model.addAttribute("matchTypes", MatchType.entries.toTypedArray())
		return "createMatch"
	}

	@GetMapping("/day")
	@ResponseBody
	fun day(@RequestParam date: LocalDate): List<String> {
		return localMatchService.getMatchNames(date)
	}

	@GetMapping("/create-t")
	fun createMatch(form: MatchCreateForm): String {
		LOGGER.info("Start creating match ${form.matchId} on ${form.matchDate} with name ${form.matchName}")
		try {
			localMatchService.createMatch(form)
		}catch (e: IllegalArgumentException) {
			LOGGER.error("Failed to create match ${form.matchId} on ${form.matchDate} with name ${form.matchName}", e)
			return "redirect:/create"
		}
		LOGGER.info("Created match ${form.matchId} on ${form.matchDate} with name ${form.matchName}")
		return "redirect:/"
	}

	@GetMapping("/get-match")
	@ResponseBody
	fun <G : Game> match(@RequestParam date: LocalDate, @RequestParam matchName: String): Match<GameFlat> {
		val match = localMatchService.getMatch(date, matchName)
		return matchFlattener.flat(match)
	}

}
