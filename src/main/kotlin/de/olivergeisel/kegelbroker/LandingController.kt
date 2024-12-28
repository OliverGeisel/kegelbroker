package de.olivergeisel.kegelbroker

import de.olivergeisel.kegelbroker.client.LiveMatchRepository
import de.olivergeisel.kegelbroker.client.LocalMatchService
import de.olivergeisel.kegelbroker.client.MatchCreateForm
import de.olivergeisel.kegelbroker.client.MatchType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/")
class LandingController(
	private val localMatchService: LocalMatchService, private val matchRepository: LiveMatchRepository
) {

	private object LOGGER {
		val log: Logger = LoggerFactory.getLogger(LandingController::class.java)
	}


	@GetMapping("/")
	fun landing(model: Model): String {
		model.addAttribute("matches", matchRepository.findAll())
		return "landing"
	}

	@GetMapping("/create")
	fun createMatch(model: Model): String {
		model.addAttribute("pointSystems", listOf("2 Teams", "Vorlauf", "Paarweise", "4 gegeneinander"))
		model.addAttribute("matchTypes", MatchType.entries.toTypedArray())
		model.addAttribute("matches", localMatchService.getMatchNames(LocalDate.now()))
		model.addAttribute("date", LocalDate.now())
		return "createMatch"
	}

	@GetMapping("/day")
	@ResponseBody
	fun day(@RequestParam date: LocalDate): List<String> {
		return localMatchService.getMatchNames(date)
	}

	@PostMapping("/create")
	fun createMatch(form: MatchCreateForm): String {
		LOGGER.log.info("Start creating match ${form.matchId} on ${form.matchDate} with name ${form.matchName}")
		try {
			localMatchService.createSpecialMatch(form)
		} catch (e: IllegalArgumentException) {
			LOGGER.log.error(
				"Failed to create match ${form.matchId} on ${form.matchDate} with name ${
					form
						.matchName
				}", e
			)
			return "redirect:/create"
		}
		LOGGER.log.info("Created match ${form.matchId} on ${form.matchDate} with name ${form.matchName}")
		return "redirect:/"
	}

	@GetMapping("/detail")
	fun detail(@RequestParam id: UUID, model: Model): String {
		val match = matchRepository.findById(id)
		if (match.isEmpty) {
			model.addAttribute("error", "Match not found")
			return "live-match-details"
		}
		model.addAttribute("match", matchRepository.findById(id).get())
		return "live-match-details"
	}

	@PostMapping("/detail")
	fun detail(@RequestParam id: UUID, @RequestParam action: String): String {
		val match = matchRepository.findById(id)
		if (match.isEmpty) {
			return "redirect:"
		}
		when (action) {
			"continue" -> {
				match.get().running = true
			}

			"end" -> {
				match.get().running = false
			}

			"static" -> {
				match.get().static = true
			}

			"update" -> {
				match.get().static = false
			}

			"reload" -> {
				val matchName = match.get().matchName
				localMatchService.reloadMatch(matchName)
			}

			"delete" -> {
				localMatchService.deleteMatch(match.get())
				return "redirect:/"
			}
		}
		matchRepository.save(match.get())
		return "redirect:/detail?id=$id"
	}

}
