package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.match.Match;
import de.olivergeisel.kegelbroker.ApplicationProperties;
import de.olivergeisel.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader;
import de.olivergeisel.kegelplay.infrastructure.data_reader.UnsupportedMatchSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

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

	@GetMapping("/get-match")
	@ResponseBody
	public <G extends Game> Match<GameFlat> match(@RequestParam String matchId) {
		try {
			Match match = localMatchService.getMatchCached(matchId);
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
		return "matchDisplay";
	}

	@GetMapping("4-against/{matchId}")
	public String display4AgainstMatch(@PathVariable("matchId") String matchId, Model model) {

		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		}
		return "matchDisplay4Against";
	}

	@GetMapping("2-against/{matchId}")
	public String display2AgainstMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		}
		return "matchDisplay2Against";
	}

	@GetMapping("vorlauf/{matchId}")
	public String displayVorlaufMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		} else {
			var dateFormatted = match.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
			var path = Path.of(applicationProperties.getDataPath()).resolve(dateFormatted).resolve(match.getMatchDir());
			try {
				var correctMatch = new KeglerheimGeneralReader(path, true).initNewMatch();
				model.addAttribute("id", matchId);
				model.addAttribute("match", correctMatch);
			} catch (UnsupportedMatchSchema e) {
				LOGGER.error("Error while reading match", e);
				model.addAttribute("error", "Error while reading match");
			}
		}
		return STR."\{templateDir}match-display-vorlauf";
	}


}
