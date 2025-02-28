package de.olivergeisel.kegelbroker.match_tree;

import jakarta.persistence.Embeddable;

@Embeddable
public record NodeConfig(int numChildren, int numPlayers) {

	public NodeConfig() {
		this(0, 0);
	}


}
