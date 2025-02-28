package de.olivergeisel.kegelbroker.match_tree;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/match-tree")
public class MatchTreeController {

	private final MatchTreeRepository matchTreeRepository;
	private final MatchNodeRepository matchNodeRepository;

	public MatchTreeController(MatchTreeRepository matchTreeRepository, MatchNodeRepository matchNodeRepository) {
		this.matchTreeRepository = matchTreeRepository;
		this.matchNodeRepository = matchNodeRepository;
	}

	@GetMapping({"", "/"})
	public String index(Model model) {
		model.addAttribute("matchTrees", matchTreeRepository.findAll());
		return "matchTree/overview";
	}

	@PostMapping("add")
	public String add(@RequestParam("name") String name) {
		var matchTree = new MatchTree(name, 2);
		matchTreeRepository.save(matchTree);
		return "redirect:/match-tree";
	}

	@GetMapping("edit")
	public String edit(@RequestParam("id") UUID id, Model model) {
		var matchTree = matchTreeRepository.findById(id).orElseThrow();
		model.addAttribute("matchTree", matchTree);
		model.addAttribute("jsonData", MatchTreeExporter.getString(matchTree));
		return "matchTree/edit";
	}

	@PostMapping("edit")
	public String edit(@RequestParam("id") UUID id, @RequestParam("name") String name) {
		var matchTree = matchTreeRepository.findById(id).orElseThrow();
		matchTree.setName(name);
		matchTreeRepository.save(matchTree);
		return "redirect:/match-tree";
	}

	@PostMapping("add-child")
	public String addChild(@RequestParam("id") UUID id, @RequestParam("name") String name,
			@RequestParam("matchTreeId") String matchTreeId) {
		var parent = matchNodeRepository.findById(id).orElseThrow();
		var child = new MatchNode2Players();
		child.setName(name);
		parent.add(child);
		matchNodeRepository.save(parent);
		return STR."redirect:edit?id=\{matchTreeId}";
	}

	@PostMapping("edit-player")
	public String editPlayer(@RequestParam("id") UUID id, @RequestParam("name") String name,
			@RequestParam("position") int position, @RequestParam("matchTreeId") String matchTreeId,
			@RequestParam("index") int index, @RequestParam("teamNumber") int teamNumber) {
		var node = matchNodeRepository.findById(id).orElseThrow();
		var player = node.getPlayers().get(position);
		player.setName(name);
		player.setTeamIndex(index);
		player.setTeamNumber(teamNumber);
		matchNodeRepository.save(node);
		return STR."redirect:edit?id=\{matchTreeId}";
	}

	@PostMapping("change-name")
	public String changeName(@RequestParam("id") UUID id, @RequestParam("name") String name,
			@RequestParam("matchTreeId") String matchTreeId) {
		var node = matchNodeRepository.findById(id).orElseThrow();
		node.setName(name);
		matchNodeRepository.save(node);
		return STR."redirect:edit?id=\{matchTreeId}";
	}

	@PostMapping("add-extra-match")
	public String addExtraMatch(@RequestParam("id") UUID id, @RequestParam("name") String name) {
		var matchTree = matchTreeRepository.findById(id).orElseThrow();
		var extraMatch = new MatchNode2Players();
		extraMatch.setName(name);
		matchTree.setExtraMatch(extraMatch);
		matchTreeRepository.save(matchTree);
		return STR."redirect:edit?id=\{id}";
	}

	@PostMapping("delete")
	public String delete(@RequestParam("id") UUID id) {
		matchTreeRepository.deleteById(id);
		return "redirect:/match-tree";
	}

	@PostMapping("save")
	public String save(@RequestParam("id") UUID id) {
		var matchTree = matchTreeRepository.findById(id).orElseThrow();
		MatchTreeExporter.export(matchTree);
		return "redirect:/match-tree";
	}

	@GetMapping("json")
	public ResponseEntity<MatchTree> json(@RequestParam("id") UUID id) {
		var matchTree = matchTreeRepository.findById(id);
		if (matchTree.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(matchTree.get());
	}

}
