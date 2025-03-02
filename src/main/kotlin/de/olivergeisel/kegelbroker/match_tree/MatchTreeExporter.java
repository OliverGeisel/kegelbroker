package de.olivergeisel.kegelbroker.match_tree;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class MatchTreeExporter {

	private static final String EXPORT_PATH = "export/";

	public static void export(MatchTree matchTree) {
		File exportDir = new File(EXPORT_PATH);
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		// json export
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(EXPORT_PATH + matchTree.getName() + ".json"), matchTree);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getString(MatchTree tree) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(tree);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
