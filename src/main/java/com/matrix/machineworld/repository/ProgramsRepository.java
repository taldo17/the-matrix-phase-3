package com.matrix.machineworld.repository;

import com.matrix.machineworld.datamodel.Program;
import com.matrix.machineworld.datamodel.ProgramForDeletion;

import java.util.List;

public interface ProgramsRepository {
    Program insert(Program program);
    Program getProgram(int programId);
    void setInactive(String id);

    void delete(String programId);

    void deletePrograms(List<Integer> ids);

    List<Program> getAllMatrixPrograms();

    void increaseDeletionCounter(int id);

    void addProgramForDeletion(ProgramForDeletion deletionCandidate);
}
