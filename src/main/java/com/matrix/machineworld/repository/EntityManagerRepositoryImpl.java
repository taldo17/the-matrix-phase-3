package com.matrix.machineworld.repository;

import com.matrix.machineworld.datamodel.Program;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class EntityManagerRepositoryImpl implements EntityManagerRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void increaseDeletionCounter(int id) {
        Program program = entityManager.find(Program.class, id);
        int currentCounter = program.getMarkedForDeletionCounter();
        program.setMarkedForDeletionCounter(++currentCounter);
        entityManager.merge(program);
    }
}
