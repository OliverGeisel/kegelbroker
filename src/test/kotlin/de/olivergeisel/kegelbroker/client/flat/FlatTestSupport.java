package de.olivergeisel.kegelbroker.client.flat;

import core.game.Game;
import core.game.GameInfo;
import core.game.GameKind;
import core.game.GameSet;
import core.match.GeneralMatchInfo;
import core.match.Match;
import core.match.MatchConfig;
import core.match.MatchStatusInfo;
import core.team_and_player.GeneralTeamInfo;
import core.team_and_player.Player;
import core.team_and_player.Team;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class FlatTestSupport {

	private FlatTestSupport() {
	}

	static Player<Game> player(String firstName, String lastName, int totalScore) {
		var player = new Player<Game>(firstName, lastName, "Test Club", "Test Team",
				LocalDate.of(2000, 1, 1));
		var game = mock(Game.class);
		var gameSet = mock(GameSet.class);
		when(game.getDate()).thenReturn(LocalDateTime.of(2026, 11, 7, 12, 0));
		when(game.getCurrentPlayer()).thenReturn(player);
		when(game.getGameSets()).thenReturn(new GameSet[]{gameSet});
		when(game.getGameInfo()).thenReturn(new GameInfo(1, 1, 1, 10.0, true));
		when(game.getGameKind()).thenReturn(GameKind.GAME_120);
		when(game.getTotalScore()).thenReturn(totalScore);
		when(gameSet.getScore()).thenReturn(totalScore);
		player.setGame(game);
		return player;
	}

	@SuppressWarnings("unchecked")
	static Team<Game> team(String name, Player<Game>... players) {
		var team = (Team<Game>) mock(Team.class);
		var teamInfo = mock(GeneralTeamInfo.class);
		var playerArray = players.clone();
		when(team.getName()).thenReturn(name);
		when(team.getGeneralTeamInfo()).thenReturn(teamInfo);
		when(team.getPlayers()).thenReturn(playerArray);
		return team;
	}

	@SuppressWarnings("unchecked")
	static Match<Game> match(Team<Game>... teams) {
		var match = (Match<Game>) mock(Match.class);
		var config = mock(MatchConfig.class);
		var generalInfo = new GeneralMatchInfo();
		var statusInfo = mock(MatchStatusInfo.class);
		var teamArray = teams.clone();
		when(match.getConfig()).thenReturn(config);
		when(match.getGeneralMatchInfo()).thenReturn(generalInfo);
		when(match.getStatusInfo()).thenReturn(statusInfo);
		when(match.getTeams()).thenReturn(teamArray);
		when(match.getPoints()).thenReturn(Map.of("Test Team", 2.0));
		when(match.getSetPoints()).thenReturn(new java.util.HashMap<>(Map.of("Test Team", 1.0)));
		when(match.getCurrentPlayers()).thenReturn(List.of());
		when(match.getGameRounds()).thenReturn(List.of());
		return match;
	}
}
