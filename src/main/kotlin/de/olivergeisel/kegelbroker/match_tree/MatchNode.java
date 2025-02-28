package de.olivergeisel.kegelbroker.match_tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public abstract class MatchNode {

	@OneToMany(cascade = CascadeType.ALL)
	private final List<MatchNode> children = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false)
	private       UUID            id;
	@ManyToOne
	@JsonIgnore
	private       MatchNode       parent;
	@Embedded
	private       NodeConfig      config;
	private       String          name     = "";

	private MatchNode() {
		config = new NodeConfig();
	}

	protected MatchNode(NodeConfig config) {
		this.config = config;
	}

	public MatchNode(String name) {
		this.name = name;
		config = new NodeConfig();
	}

	public void add(MatchNode2Players child) {
		if (config.numChildren() > children.size()) {
			children.add(child);
			child.setParent(this);
		} else {
			throw new IllegalArgumentException("Cannot add more children to this node");
		}
	}

	public MatchNode set(MatchNode2Players child, int index) {
		if (index < 0 || index >= config.numChildren()) {
			throw new IllegalArgumentException("Index out of bounds");
		}
		child.setParent(this);
		return children.set(index, child);
	}

	//region setter/getter
	public int getLevel() {
		if (parent == null) {
			return 1;
		}
		return parent.getLevel() + 1;
	}

	public MatchNode getParent() {
		return parent;
	}

	public void setParent(MatchNode parent) {
		this.parent = parent;
	}

	public NodeConfig getConfig() {
		return config;
	}

	public void setConfig(NodeConfig config) {
		this.config = config;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getId() {return id;}

	public abstract List<MatchTreePlayer> getPlayers();

	/**
	 * Get the depth of the tree.
	 * The minimum depth is 1.
	 *
	 * @return the depth of the tree
	 */
	public int getDepth() {
		var depth = 0;
		if (children == null) {
			return 1;
		}
		for (var child : children) {
			depth = Math.max(depth, child.getDepth());
		}
		return depth + 1;
	}

	public int getNumberNodes() {
		if (children == null) {
			return 1;
		}
		var numberNodes = 1;
		for (var child : children) {
			numberNodes += child.getNumberNodes();
		}
		return numberNodes;
	}

	public List<MatchNode> getChildren() {
		return children;
	}

	public abstract void setChildren(List<MatchNode> children);

	public abstract MatchTreePlayer[] getWinners();

	public abstract MatchTreePlayer[] getLosers();
//endregion

}
