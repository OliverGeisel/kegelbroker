package de.olivergeisel.kegelbroker.client

import core.game.Game
import core.game.Game120
import core.match.Match
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate

@Controller
class LandingController(private val localMatchService: LocalMatchService,
	private val matchFlattener: MatchFlattener<Game120>) {


	@GetMapping("/")
	fun landing(model: Model): String {
		model.addAttribute("matches", listOf("match1", "match2"))
		return "landing"
	}

	@GetMapping("/create")
	fun createMatch(model: Model): String {
		return "createMatch"
	}

	@GetMapping("/day")
	@ResponseBody
	fun day(@RequestParam date:LocalDate): List<String> {
		return localMatchService.getMatchNames(date)
	}

	@PostMapping("/create")
	fun createMatch( form:MatchCreateForm): String {
		return "redirect:/"
	}

	@GetMapping("/get-match")
	@ResponseBody
	fun <G: Game>match(@RequestParam date:LocalDate, @RequestParam matchName:String): Match<GameFlat> {
		var match =  localMatchService.getMatch(date, matchName)
		return matchFlattener.flat(match)
	}

}
