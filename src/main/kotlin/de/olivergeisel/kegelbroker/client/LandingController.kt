package de.olivergeisel.kegelbroker.client

import core.game.Game120
import de.olivergeisel.kegelbroker.match_tree.MatchNode
import de.olivergeisel.kegelbroker.match_tree.MatchTree
import de.olivergeisel.kegelbroker.match_tree.MatchTreeImporter
import de.olivergeisel.kegelbroker.match_tree.MatchTreeRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import java.util.*

@Controller
@PreAuthorize("hasRole('ADMIN')")
class LandingController(
	private val localMatchService: LocalMatchService,
	private val matchFlattener: MatchFlattener<Game120>,
	private val matchRepository: LiveMatchRepository,
	private val matchTreeRepository: MatchTreeRepository
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

	@PostMapping("/create")
	fun createMatch(form: MatchCreateForm): String {
		LOGGER.info("Start creating match ${form.matchId} on ${form.matchDate} with name ${form.matchName}")
		try {
			localMatchService.createMatch(form)
		} catch (e: IllegalArgumentException) {
			LOGGER.error("Failed to create match ${form.matchId} on ${form.matchDate} with name ${form.matchName}", e)
			return "redirect:/create"
		}
		LOGGER.info("Created match ${form.matchId} on ${form.matchDate} with name ${form.matchName}")
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
		model.addAttribute("matchTrees", MatchTreeImporter.readMatchTrees())
		return "live-match-details"
	}

	private fun fixChildren(nodes: List<MatchNode>, parent: MatchNode): Unit {
		for (node in nodes) {
			node.children.forEach {
				it.parent = parent
				fixChildren(it.children, it)
			}
		}
	}

	private fun fixingTree(tree: MatchTree): Unit {
		fixChildren(tree.root.children, tree.root)
	}

	@PostMapping("/add-tree")
	fun addTree(@RequestParam id: UUID, @RequestParam treeId: String): String {
		val match = matchRepository.findById(id).get()
		var tree = MatchTreeImporter.loadBlank(treeId)
		tree = matchTreeRepository.save(tree)
		fixingTree(tree)
		tree = matchTreeRepository.save(tree)
		match.matchTree.add(tree)
		matchRepository.save(match)
		return "redirect:detail?id=${id}"
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
