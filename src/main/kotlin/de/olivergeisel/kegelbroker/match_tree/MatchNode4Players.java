package de.olivergeisel.kegelbroker.match_tree;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

import java.util.List;

@Entity
public class MatchNode4Players extends MatchNode {
	@OneToOne(cascade = CascadeType.ALL)
	private MatchTreePlayer player1;
	@OneToOne(cascade = CascadeType.ALL)
	private MatchTreePlayer player2;
	@OneToOne(cascade = CascadeType.ALL)
	private MatchTreePlayer player3;
	@OneToOne(cascade = CascadeType.ALL)
	private MatchTreePlayer player4;

	protected MatchNode4Players() {
		super(new NodeConfig(4, 4));
		player1 = new MatchTreePlayer();
		player2 = new MatchTreePlayer();
		player3 = new MatchTreePlayer();
		player4 = new MatchTreePlayer();
	}

	public MatchNode4Players(MatchTreePlayer player1, MatchTreePlayer player2, MatchTreePlayer player3,
			MatchTreePlayer player4) {
		super(new NodeConfig(4, 4));
		this.player1 = player1;
		this.player2 = player2;
		this.player3 = player3;
		this.player4 = player4;
	}

	//region setter/getter
	public MatchTreePlayer getPlayer1() {
		return player1;
	}

	public void setPlayer1(MatchTreePlayer player1) {
		this.player1 = player1;
	}

	public MatchTreePlayer getPlayer2() {
		return player2;
	}

	public void setPlayer2(MatchTreePlayer player2) {
		this.player2 = player2;
	}

	public MatchTreePlayer getPlayer3() {
		return player3;
	}

	public void setPlayer3(MatchTreePlayer player3) {
		this.player3 = player3;
	}

	public MatchTreePlayer getPlayer4() {
		return player4;
	}

	public void setPlayer4(MatchTreePlayer player4) {
		this.player4 = player4;
	}

	@Override
	public List<MatchTreePlayer> getPlayers() {
		return List.of(player1, player2, player3, player4);
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
