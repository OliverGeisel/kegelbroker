package de.olivergeisel.kegelbroker.match_tree;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

import java.util.List;

@Entity
public class MatchNode2Players extends MatchNode {
	@OneToOne(cascade = CascadeType.ALL)
	private MatchTreePlayer player1;
	@OneToOne(cascade = CascadeType.ALL)
	private MatchTreePlayer player2;

	protected MatchNode2Players() {
		super(new NodeConfig(2, 2));
		player1 = new MatchTreePlayer();
		player2 = new MatchTreePlayer();

	}
	public MatchNode2Players(MatchTreePlayer player1, MatchTreePlayer player2) {
		super(new NodeConfig(2, 2));
		this.player1 = player1;
		this.player2 = player2;
	}

//region setter/getter
	public MatchTreePlayer getPlayer1() {
		return player1;
	}

	public MatchTreePlayer getPlayer2() {
		return player2;
	}

	@Override
	public List<MatchTreePlayer> getPlayers() {
		return List.of(player1, player2);
	}

	@Override
	public MatchTreePlayer[] getWinners() {
		return new MatchTreePlayer[0];
	}

	@Override
	public MatchTreePlayer[] getLosers() {
		return new MatchTreePlayer[0];
	}

	@Override
	public void setChildren(List<MatchNode> children) {

	}
//endregion


}
