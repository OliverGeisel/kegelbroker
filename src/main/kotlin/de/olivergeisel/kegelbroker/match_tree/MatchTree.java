package de.olivergeisel.kegelbroker.match_tree;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class MatchTree {

	@OneToOne(cascade = CascadeType.ALL)
	MatchNode root;
	@OneToOne(cascade = CascadeType.ALL)
	MatchNode extraMatch;
	private String name;
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false)
	private UUID   id;
	private int    maxDepth = 4;

	protected MatchTree() {
	}

	public MatchTree(String name, int numberOfPlayersInRoot) {
		if (numberOfPlayersInRoot == 2) {
			root = new MatchNode2Players();
			root.setName("Root");
		} else if (numberOfPlayersInRoot == 4) {
			root = new MatchNode4Players();
		} else {
			throw new IllegalArgumentException("Number of players in root must be 2 or 4");
		}
		this.name = name;
	}

	public MatchTree(String name, MatchNode root) {
		this.name = name;
		this.root = root;
	}

	//region setter/getter
	public int getMaxDepth() {
		return maxDepth;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getId() {return id;}

	public MatchNode getExtraMatch() {
		return extraMatch;
	}

	public void setExtraMatch(MatchNode extraMatch) {
		this.extraMatch = extraMatch;
	}

	public MatchNode getRoot() {
		return root;
	}

	/**
	 * Get the depth of the tree.
	 *
	 * @return
	 */
	public int getDepth() {
		return root.getDepth();
	}

	public int getNumberNodes() {
		return root.getNumberNodes();
	}
//endregion

}
