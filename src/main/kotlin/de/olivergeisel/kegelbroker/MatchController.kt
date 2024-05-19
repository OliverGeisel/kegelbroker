package de.olivergeisel.kegelbroker

import core.game.Game
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.util.HtmlUtils
import core.match.*;


@Controller
class MatchController(private val messagingTemplate: SimpMessagingTemplate) {





	fun getMatch(): Match<Game> {
		return Match2Teams<Game>(null,null,null,null,null,null,null)
	}

	@GetMapping("/match")
	@ResponseBody
	fun greeting(@RequestParam name: String, match: Match<Game>) {
		Thread.sleep(1000) // simulated delay
		val t = Message("Hello, ${HtmlUtils.htmlEscape(name)}!")
		messagingTemplate.convertAndSend("/ws-response/greetings", t)
	}
}