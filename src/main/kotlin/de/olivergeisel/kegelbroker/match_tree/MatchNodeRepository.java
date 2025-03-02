package de.olivergeisel.kegelbroker.match_tree;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface MatchNodeRepository extends CrudRepository<MatchNode, UUID> {


	@Override
	Streamable<MatchNode> findAll();
}
