package de.olivergeisel.kegelbroker.match_tree;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface MatchTreeRepository extends CrudRepository<MatchTree, UUID> {

	@Override
	Streamable<MatchTree> findAll();
}
