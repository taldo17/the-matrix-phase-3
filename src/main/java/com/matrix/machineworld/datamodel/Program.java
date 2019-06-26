package com.matrix.machineworld.datamodel;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
public class Program {
    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String purpose;
    private LocalDate creationDate;
    private LocalDate lastActive;
    private double memoryConsumption;
    private double cpuUsage;
    private String creator;
    private ProgramCategory category;
    private int markedForDeletionCounter;
    private boolean active;
    @ElementCollection(targetClass=String.class)
    private List<String> suspectedMaliciousIntentions;
    @ElementCollection(targetClass=String.class)
    private List<String> abnormalBehaviours;
}
