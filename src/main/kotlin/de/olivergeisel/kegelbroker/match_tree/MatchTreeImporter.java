package de.olivergeisel.kegelbroker.match_tree;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class MatchTreeImporter {

	private static final String IMPORT_PATH = "import/";

	private static void fixChildren(MatchNode node) {
		node.getChildren().forEach(c -> {
			c.setParent(node);
			fixChildren(c);
		});
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
