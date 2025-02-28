package de.olivergeisel.kegelbroker.match_tree;

import core.game.Game;
import core.team_and_player.Player;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class MatchTreePlayer {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false)
	private UUID id;

	private String name      = "";
	private int    score;
	private double points;
	private int    teamIndex = -1;
	private int teamNumber = -1;

	protected MatchTreePlayer() {
	}

	public MatchTreePlayer(String name, int score, double points) {
		this.name = name;
		this.score = score;
		this.points = points;
	}

	public static <G extends Game> MatchTreePlayer fromPlayer(Player<G> player) {
		return new MatchTreePlayer(player.getCompleteName(), player.getGame().getTotalScore(), 0);
	}

	//region setter/getter
	public int getTeamNumber() {
		return teamNumber;
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	public UUID getId() {return id;}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTeamIndex() {
		return teamIndex;
	}

	public void setTeamIndex(int teamIndex) {
		this.teamIndex = teamIndex;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		this.points = points;
	}
//endregion
}
