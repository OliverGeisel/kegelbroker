package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.game.GameKind;
import core.match.Match;
import de.olivergeisel.kegelbroker.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/live")
public class MatchDisplayController {

	private final static Logger LOGGER = LoggerFactory.getLogger(MatchDisplayController.class);
	private static final String templateDir = "matchDisplay/";

	private final LiveMatchRepository            liveMatchRepository;
	private final MatchFlattener<? extends Game> matchFlattener;
	private final LocalMatchService              localMatchService;

	@Autowired
	ApplicationProperties applicationProperties;

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
	public String displayMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		}
		return STR."\{templateDir}empty";// "matchDisplay";
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
