package de.olivergeisel.kegelbroker.match_tree;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MatchTreeImporter {

	private static final String IMPORT_PATH = "import/";

	private static void fixChildren(MatchNode node) {
		node.getChildren().forEach(c -> {
			c.setParent(node);
			fixChildren(c);
		});
	}

	public static List<String> readMatchTrees() {
		var folder = new File(IMPORT_PATH);
		var back = new LinkedList<String>();
		for (var file : folder.listFiles()) {
			back.add(file.getName());
		}
		return back;
	}

	private static void overrideId(MatchNode node) {
		try {
			var field = node.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(node, UUID.randomUUID());
			field.setAccessible(false);
		} catch (NoSuchFieldException nef) {

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeChildren(List<MatchNode> nodes) {
		if (nodes == null) {
			return;
		}
		for (var node : nodes) {
			overrideId(node);
			writeChildren(node.getChildren());
		}
	}

	public static MatchTree loadBlank(String name) {
		var tree = importMatchTree(name);
		// reassign the IDs
		var root = tree.root;
		overrideId(root);
		writeChildren(root.getChildren());
		return tree;
	}

	public static MatchTree importMatchTree(String name) {
		// json import
		ObjectMapper objectMapper = new ObjectMapper();
		// fix path
		name = name.replace(".json", "");
		name = name.replace(".JSON", "");
		try {
			var back = objectMapper.readValue(new File(STR."\{IMPORT_PATH}\{name}.json"), MatchTree.class);
			// fix parent
			fixChildren(back.getRoot());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
