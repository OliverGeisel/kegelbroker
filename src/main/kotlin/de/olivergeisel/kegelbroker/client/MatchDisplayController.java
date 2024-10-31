package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.game.GameKind;
import core.match.Match;
import de.olivergeisel.kegelbroker.client.flat.GameFlat;
import de.olivergeisel.kegelbroker.client.flat.MatchFlat;
import de.olivergeisel.kegelbroker.client.flat.MatchFlattener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/live")
public class MatchDisplayController {

	private final static Logger                 LOGGER      = LoggerFactory.getLogger(MatchDisplayController.class);
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

	public MatchDisplayController(LiveMatchRepository liveMatchRepository, MatchFlattener<Game> matchFlattener,
			LocalMatchService localMatchService) {
		this.liveMatchRepository = liveMatchRepository;
		this.matchFlattener = matchFlattener;
		this.localMatchService = localMatchService;
	}

	private String matchNotFound(String matchId) {
		LOGGER.warn(STR."Requested match \{matchId} not exsist");
		return STR."\{templateDir}empty";
	}

	@GetMapping({"", "/"})
	public String overview(@RequestParam(required = false, value = "day") LocalDate day, Model model) {
		var matchesToday =
				day == null ? localMatchService.getAllMatchesToday() : localMatchService.getAllMatchesOf(day);
		model.addAttribute("liveMatches", matchesToday.filter(LiveMatch::getRunning));
		model.addAttribute("completedMatches", matchesToday.filter(it -> !it.getRunning() || it.getStatic()));
		model.addAttribute("templates", templates.keySet());
		return STR."\{templateDir}overview";
	}


	/**
	 * Get a {@link MatchFlat} by id of the {@link LiveMatch}.
	 *
	 * @param matchId the id of the match
	 * @return the match if it exists, otherwise null
	 */
	@GetMapping("/get-match")
	@ResponseBody
	public <G extends Game> Match<GameFlat> match(@RequestParam String matchId) {
		try {
			Match<?> match = localMatchService.getMatchCached(matchId);
			return matchFlattener.flat(match);
		} catch (IllegalArgumentException e) {
			LOGGER.warn(STR."Requested match \{matchId} not exsist");
			return null;
		}
	}

	@GetMapping("{matchId}")
	public String displayMatch(@PathVariable("matchId") String matchId, @RequestParam("view") String view,
			Model model) {
		var match = liveMatchRepository.findByMatchNameIgnoreCase(matchId);
		var template = getTemplate(view);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		}
		var correctMatch = localMatchService.getMatchCached(matchId);
		model.addAttribute("match", correctMatch);
		model.addAttribute("matchName", matchId);
		return STR."\{templateDir}\{template}";// "matchDisplay";
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
			return STR."\{templateDir}empty";
		}
		return STR."\{templateDir}empty";//"matchDisplay4Against";
	}

	@GetMapping("2-teams/{matchId}")
	public String display2TeamsMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			return matchNotFound(matchId);
		}
		var correctMatch = localMatchService.getMatchCached(matchId);
		model.addAttribute("match", correctMatch);
		model.addAttribute("id", matchId);
		return STR."\{templateDir}match-display-2-teams";
	}

	@GetMapping("2-against/{matchId}")
	public String display2AgainstMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		}
		return STR."\{templateDir}empty";// "matchDisplay2Against";
	}

	@GetMapping("vorlauf/{matchId}")
	public String displayVorlaufMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		Match correctMatch;
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		} else {
			correctMatch = localMatchService.getMatchCached(matchId);
			model.addAttribute("id", matchId);
			model.addAttribute("match", correctMatch);
		}
		if (correctMatch.getConfig().getKind() == GameKind.GAME_40) {
			return STR."\{templateDir}match-display-vorlauf-sprint";
		}
		return STR."\{templateDir}match-display-vorlauf";
	}

}
