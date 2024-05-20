package de.olivergeisel.kegelbroker.client;

import core.game.Game;
import core.match.Match1Team;
import de.olivergeisel.kegelbroker.ApplicationProperties;
import de.olivergeisel.kegelplay.infrastructure.data_reader.KeglerheimGeneralReader;
import de.olivergeisel.kegelplay.infrastructure.data_reader.MatchNTeams;
import de.olivergeisel.kegelplay.infrastructure.data_reader.UnsupportedMatchSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Controller
public class MatchDisplayController {

	private final static Logger LOGGER = LoggerFactory.getLogger(MatchDisplayController.class);
	private static final String templateDir = "matchDisplay/";

	private final LiveMatchRepository  liveMatchRepository;
	private final MatchFlattener<Game> matchFlattener;
	@Autowired
	ApplicationProperties applicationProperties;

	public MatchDisplayController(LiveMatchRepository liveMatchRepository, MatchFlattener<Game> matchFlattener) {
		this.liveMatchRepository = liveMatchRepository;
		this.matchFlattener = matchFlattener;
	}

	@GetMapping("/live/{matchId}")
	public String displayMatch() {
		return "matchDisplay";
	}

	@GetMapping("/live/4-against/{matchId}")
	public String display4AgainstMatch() {
		return "matchDisplay4Against";
	}

	@GetMapping("/live/2-against/{matchId}")
	public String display2AgainstMatch() {
		return "matchDisplay2Against";
	}

	@GetMapping("/live/vorlauf/{matchId}")
	public String displayVorlaufMatch(@PathVariable("matchId") String matchId, Model model) {
		var match = liveMatchRepository.findByMatchName(matchId);
		if (match == null) {
			model.addAttribute("error", "Match not found. Nice Try! ;)");
			return STR."\{templateDir}empty";
		}else {
			var dateFormatted = match.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
			var path = Path.of(applicationProperties.getDataPath()).resolve(dateFormatted).resolve(match.getMatchDir());
			try {
				var correctMatch = (Match1Team) new KeglerheimGeneralReader(path, true).initNewMatch();
				model.addAttribute("match", correctMatch);
			} catch (UnsupportedMatchSchema e) {
				LOGGER.error("Error while reading match", e);
				model.addAttribute("error", "Error while reading match");
			}
		}
		return STR."\{templateDir}match-display-vorlauf";
	}


}
