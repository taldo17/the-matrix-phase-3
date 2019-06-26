package com.matrix.machineworld.datamodel;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ProgramForDeletion {
    @Id
    private int programId;
    private DeletionReason potentialDeletionReason;
}
