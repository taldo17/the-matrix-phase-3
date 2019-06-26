package com.matrix.machineworld.repository;

import com.matrix.machineworld.datamodel.ProgramForDeletion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramForDeletionRepository extends CrudRepository<ProgramForDeletion, Integer> {
}
