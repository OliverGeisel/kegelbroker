package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.game.GameKind;
import core.match.Match;
import de.olivergeisel.kegelbroker.client.flat.GameFlat;
import de.olivergeisel.kegelbroker.client.flat.MatchFlat;
import de.olivergeisel.kegelbroker.client.flat.MatchFlattener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Controller to handle all live-views of all currently running {@link LiveMatch}s.
 *
 * @author Oliver Geisel
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/live")
public class LiveMatchController {

	private final static Logger                 LOGGER      = LoggerFactory.getLogger(LiveMatchController.class);
	private static final String                 templateDir = "matchDisplay/";
	private static final Map<MatchType, String> templates   = Map.of(
			MatchType.FINALE, "finale",
			MatchType.HALBFINALE, "match-display-halbfinale",
			MatchType.VORLAUF, "match-display-vorlauf",
			MatchType.TEAMS2_6S_120, "match-display-2-teams",
			MatchType.TEAMS2_4S_120, "match-display-2-teams-alt1"
	);

	private final LiveMatchRepository            liveMatchRepository;
	private final MatchFlattener<? extends Game> matchFlattener;
	private final LocalMatchService              localMatchService;

	public LiveMatchController(LiveMatchRepository liveMatchRepository, MatchFlattener<Game> matchFlattener,
			LocalMatchService localMatchService) {
		this.liveMatchRepository = liveMatchRepository;
		this.matchFlattener = matchFlattener;
		this.localMatchService = localMatchService;
	}

	private String matchNotFound(String matchId) {
		LOGGER.warn("Requested match '{}' not exsist", matchId);
		return templateDir + "empty";
	}

	@GetMapping({"", "/"})
	public String overview(@RequestParam(required = false, value = "day") LocalDate day, Model model) {
		var matchesToday =
				day == null ? localMatchService.getAllMatchesToday() : localMatchService.getAllMatchesOf(day);
		model.addAttribute("liveMatches", matchesToday.filter(it -> it.getRunning() && !it.getStatic()));
		model.addAttribute("completedMatches", matchesToday.filter(it -> !it.getRunning() || it.getStatic()));
		model.addAttribute("templates", templates.keySet());
		return templateDir + "overview";
	}

	@GetMapping("edit-select")
	@PreAuthorize("hasRole('ADMIN')")
	public String editSelect(Model model) {
		var allMatches = liveMatchRepository.findAll()
											.stream()
											.sorted((first, second) -> {
												int dateComparison = second.getDate().compareTo(first.getDate());
												return dateComparison != 0 ? dateComparison :
														first.getMatchName().compareToIgnoreCase(
																second.getMatchName());
											});
		model.addAttribute("matches", allMatches.toList());
		return templateDir + "edit-select";
	}

	@GetMapping("{matchId}/details")
	String matchDetails(@PathVariable("matchId") String matchName, Model model) {
		var match = liveMatchRepository.findByMatchNameIgnoreCase(matchName);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return String.format("%sempty", templateDir);
		}
		model.addAttribute("match", match);
		model.addAttribute("matchName", matchName);
		model.addAttribute("matchId", match.getId());
		return String.format("%sdetails", templateDir);
	}


	/**
	 * Display a match by its id and the view to use.
	 *
	 * @param matchId id of the match to display
	 * @param view    view to use for displaying the match. Must be one of the {@link MatchType} values.
	 * @param model   model to add attributes to
	 *
	 * @return the name of the template to use for displaying the match
	 */
	@GetMapping("{matchId}")
	public String displayMatch(@PathVariable("matchId") String matchId, @RequestParam("view") String view,
			Model model) {
		var match = liveMatchRepository.findByMatchNameIgnoreCase(matchId);
		var template = getTemplate(view);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return String.format("%sempty", templateDir);
		}
		var correctMatch = localMatchService.getMatchCached(matchId);
		model.addAttribute("match", correctMatch);
		model.addAttribute("matchName", matchId);
		return String.format("%s%s", templateDir, template);
	}

	/**
	 * Edit a match by its id. This will display the edit view for the match.
	 *
	 * @param matchId id of the match to edit
	 * @param model   model to add attributes to
	 *
	 * @return the name of the template to use for editing the match
	 */
	@GetMapping("edit")
	@PreAuthorize("hasRole('ADMIN')")
	public String editMatch(@RequestParam(value = "matchId", required = false) UUID matchId, Model model,
			RedirectAttributes redirectAttributes) {
		if (matchId == null) {
			return editSelect(model);
		}
		var match = liveMatchRepository.findById(matchId);
		if (match.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Match not found.");
			return "redirect:/live/edit-select";
		}
		model.addAttribute("match", match.orElseThrow());
		model.addAttribute("matchTypes", MatchType.getEntries());
		return templateDir + "edit";
	}

	@PostMapping("edit")
	@PreAuthorize("hasRole('ADMIN')")
	public String updateMatch(@RequestParam("matchId") UUID matchId, @RequestParam("matchName") String matchName,
			@RequestParam("date") LocalDate date, @RequestParam("matchDir") String matchDir,
			@RequestParam("matchType") MatchType matchType,
			@RequestParam(name = "running", defaultValue = "false") boolean running,
			@RequestParam(name = "static", defaultValue = "false") boolean isStatic,
			@RequestParam(name = "special", defaultValue = "false") boolean special,
			RedirectAttributes redirectAttributes) {
		try {
			localMatchService.updateMatch(matchId, matchName, date, matchDir, matchType, running, isStatic, special);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addAttribute("matchId", matchId);
			return "redirect:/live/edit";
		}
		redirectAttributes.addFlashAttribute("message", "Match wurde gespeichert.");
		redirectAttributes.addAttribute("matchId", matchName.trim());
		return "redirect:/live/edit";
	}

	private String getTemplate(String name) {
		if (name == null || name.isBlank()) {
			return "empty";
		}
		try {
			var type = MatchType.valueOf(name);
			return getTemplate(type);
		} catch (IllegalArgumentException e) {
			return "empty";
		}

	}

	private String getTemplate(MatchType type) {
		return templates.getOrDefault(type, "empty");
	}

	@GetMapping("4-against/{matchId}")
	public String display4AgainstMatch(@PathVariable("matchId") String matchId, Model model) {

		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return String.format("%sempty", templateDir);
		}
		return String.format("%sempty", templateDir);//"matchDisplay4Against";
	}

	@GetMapping("2-teams/{matchId}")
	public String display2TeamsMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			return matchNotFound(matchId);
		}
		var correctMatch = localMatchService.getMatchCached(matchId);
		model.addAttribute("match", correctMatch);
		model.addAttribute("matchName", correctMatch);
		model.addAttribute("id", matchId);
		return String.format("%smatch-display-2-teams", templateDir);
	}

	@GetMapping("2-against/{matchId}")
	public String display2AgainstMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return String.format("%sempty", templateDir);
		}
		return String.format("%sempty", templateDir);// "matchDisplay2Against";
	}

	@GetMapping("vorlauf/{matchId}")
	public String displayVorlaufMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		Match<?> correctMatch;
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return String.format("%sempty", templateDir);
		} else {
			correctMatch = localMatchService.getMatchCached(matchId);
			model.addAttribute("id", matchId);
			model.addAttribute("match", correctMatch);
		}
		if (correctMatch.getConfig().getKind() == GameKind.GAME_40) {
			return String.format("%smatch-display-vorlauf-sprint", templateDir);
		}
		return String.format("%smatch-display-vorlauf", templateDir);
	}

	@GetMapping("24-hours/{matchId}")
	public String get24Hours(@PathVariable String matchId, Model model) {
		var match = liveMatchRepository.findByMatchNameIgnoreCase(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return String.format("%sempty", templateDir);
		}
		var correctMatch = localMatchService.getMatchCached(matchId);
		model.addAttribute("match", correctMatch);
		model.addAttribute("matchName", matchId);
		return String.format("%s24-hours", templateDir);
	}

	/**
	 * Get a {@link MatchFlat} by id of the {@link LiveMatch}.
	 *
	 * @param matchId the id of the match
	 *
	 * @return the match if it exists, otherwise null
	 */
	@GetMapping("/get-match")
	@ResponseBody
	public Match<GameFlat> match(@RequestParam String matchId) {
		try {
			Match<?> match = localMatchService.getMatchCached(matchId);
			return matchFlattener.flat(match);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Requested match {} not exist", matchId);
			return null;
		}
	}
}
