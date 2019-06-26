package com.matrix.machineworld.service;

import com.matrix.machineworld.datamodel.*;
import com.matrix.machineworld.repository.ProgramsRepository;
import com.matrix.machineworld.service.notrelevant.LoginResponse;
import com.matrix.machineworld.service.notrelevant.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MachineServiceImpl implements MachineService {
    private Logger logger = LoggerFactory.getLogger(MachineServiceImpl.class);

    private final ProgramsRepository programsRepository;
    private LoginService loginService;
    //ToDo change that to a thread safe collection
    List<Program> LastObservation = null;


    @Autowired
    public MachineServiceImpl(ProgramsRepository programsRepository, LoginService loginService) {
        this.programsRepository = programsRepository;
        this.loginService = loginService;
    }


    @Override
    public int insert(Program program) {
        try {
            validateProgram(program);
            logger.info("inserting program: " + program.toString());
            Program savedProgram = programsRepository.insert(program);
            logger.info("inserted program: " + program.toString());
            return savedProgram.getId();
        }
        catch (DataAccessException e) {
            logger.error("a data access error occurred", e);
            throw new MachineWorldGeneralException(e);
        }
        catch (Exception e) {
            logger.error("an error occurred while trying to insert a program", e);
            throw new MachineWorldGeneralException(e);
        }
    }

    @Override
    public Program getProgram(int programId) {
        try {
            validateId(programId);
            return programsRepository.getProgram(programId);
        }
        catch (DataAccessException e) {
            logger.error("an error occurred while accessing the DB", e);
            throw new MachineWorldUnavailableException(e);
        }
        catch (Exception e) {
            logger.error("an error occurred while trying to get a program", e);
            throw new MachineWorldGeneralException(e);
        }
    }

    @Override
    public DResponse deactivate(DRequest dRequest) {
        DResponse dResponse = new DResponse();
        try {
            validateDR(dRequest);
            tryToLogin(dRequest.toString(),
                    dRequest.getUserName(),
                    dRequest.getUserSecret());
            logger.info("setting program +" +
                    dRequest.getProgramId() + " inactive");
            programsRepository.setInactive(dRequest.getProgramId());
            dResponse.setSucceeded(true);
            dResponse.setActionReport("deactivated successfully");
            logger.info(" program +" +
                    dRequest.getProgramId() + " is deactivated");
        }
        catch (IllegalAccessException e) {
            logger.error("failed to login in order to perform deactivation");
            throw new MachineWorldUnauthorizedException(e);
        }
        catch (DataAccessException e) {
            logger.error("failed to access the DB");
            throw new MachineWorldUnavailableException(e);
        }
        catch (Exception e) {
            logger.error("failed to deactivate the program");
            throw new MachineWorldGeneralException(e);
        }

        return null;
    }

    @Override
    public DResponse delete(DRequest dRequest) {
        DResponse dResponse = new DResponse();
        try {
            validateDR(dRequest);
            tryToLogin(dRequest.toString(),
                    dRequest.getUserName(),
                    dRequest.getUserSecret());
            logger.info("deleting program +" +
                    dRequest.getProgramId());
            programsRepository.delete(dRequest.getProgramId());
            dResponse.setSucceeded(true);
            dResponse.setActionReport("deleted successfully");
            logger.info("program +" +
                    dRequest.getProgramId() + " is deleted");
        }
        catch (IllegalAccessException e) {
            logger.error("failed to login in order to perform deletion");
            throw new MachineWorldUnauthorizedException(e);
        }
        catch (DataAccessException e) {
            logger.error("failed to access the DB");
            throw new MachineWorldUnavailableException(e);
        }
        catch (Exception e) {
            logger.error("failed to delete the program");
            throw new MachineWorldGeneralException(e);
        }
        return dResponse;
    }

    @Override
    public DResponse deletePrograms(DRequest dRequest) {
        DResponse dResponse = new DResponse();
        try {
            validateDR(dRequest);
            tryToLogin(dRequest.toString(),
                    dRequest.getUserName(),
                    dRequest.getUserSecret());
            logger.info("deleting " + dRequest.getIds().size() + " programs");
            programsRepository.deletePrograms(dRequest.getIds());
            dResponse.setSucceeded(true);
            dResponse.setActionReport("deleted successfully");
            logger.info("deleting " + dRequest.getIds().size() + " programs successfully");
        }
        catch (IllegalAccessException e) {
            logger.error("failed to login in order to perform deletion");
            throw new MachineWorldUnauthorizedException(e);
        }
        catch (DataAccessException e) {
            logger.error("failed to access the DB");
            throw new MachineWorldUnavailableException(e);
        }
        catch (Exception e) {
            logger.error("failed to delete the program");
            throw new MachineWorldGeneralException(e);
        }
        return dResponse;
    }

    @Override
    public void activateGarbageCollection() {
        //stop the world
        stopTheWorld();
        //Collections
        List<Program> allMatrixPrograms = programsRepository.getAllMatrixPrograms();
        List<Program> programsForDeletion = new ArrayList<>();
        List<Program> programsWithHighMemory = new ArrayList<>();
        List<Program> programsWithHighCPU = new ArrayList<>();
        List<Program> buggyPrograms = new ArrayList<>();
        List<Program> maliciousPrograms = new ArrayList<>();

        //This part handles the situation when we have a situation where there was
        //a last observation
        if (LastObservation != null) {
            for (Program program : allMatrixPrograms) {
                int id = program.getId();
                Program programFromPreviousRun = LastObservation.get(id);
                if (program.getMemoryConsumption() > programFromPreviousRun.getMemoryConsumption()) {
                    double memoryConsumption1 = program.getMemoryConsumption();
                    double memoryConsumption2 = programFromPreviousRun.getMemoryConsumption();
                    double difference = memoryConsumption1 - memoryConsumption2;

                    //check if there was an increase of more than 20 percentage
                    if ((difference / memoryConsumption1) * 100 > 20) {
                        program.setMarkedForDeletionCounter(program.getMarkedForDeletionCounter() + 1);
                        if (program.getMarkedForDeletionCounter() > 5) {
                            programsRepository.setInactive(String.valueOf(program.getId()));
                            programsWithHighMemory.add(program);
                        }
                        else {
                            programsRepository.increaseDeletionCounter(program.getId());
                        }
                    }
                }
                if (program.getCpuUsage() > programFromPreviousRun.getCpuUsage()) {
                    double cpuUsage1 = program.getCpuUsage();
                    double cpuUsage2 = programFromPreviousRun.getCpuUsage();
                    double difference = cpuUsage1 - cpuUsage2;

                    //check if there was an increase of more than 15 percentage
                    if ((difference / cpuUsage1) * 100 > 15) {

                        program.setMarkedForDeletionCounter(program.getMarkedForDeletionCounter() + 1);
                        if (program.getMarkedForDeletionCounter() > 5) {
                            programsRepository.setInactive(String.valueOf(program.getId()));
                            programsWithHighCPU.add(program);
                        }
                        else {
                            programsRepository.increaseDeletionCounter(program.getId());
                        }
                    }
                }
            }
        }

        //assign the last observation with the new
        LastObservation = allMatrixPrograms;
        for (Program program : allMatrixPrograms) {

            int forbiddenBehaviourCounter = 0;
            for (String potentialMaliciousBehaviour : program.getSuspectedMaliciousIntentions()) {
                if (potentialMaliciousBehaviour.contains("Violence") || potentialMaliciousBehaviour.contains("Evil")) {
                    forbiddenBehaviourCounter++;
                }
            }

            if (forbiddenBehaviourCounter > 5) {
                if (program.getMarkedForDeletionCounter() > 5) {
                    programsRepository.setInactive(String.valueOf(program.getId()));
                    maliciousPrograms.add(program);
                }
                else {
                    programsRepository.increaseDeletionCounter(program.getId());
                }

            }

            int abnormalSuspiciousBehaviour = 0;
            for (String abnormalBehaviour : program.getAbnormalBehaviours()) {
                if (abnormalBehaviour.contains("Crazy") || abnormalBehaviour.contains("Confused")) {
                    abnormalSuspiciousBehaviour++;
                }
            }

            if (abnormalSuspiciousBehaviour > 5) {
                if (program.getMarkedForDeletionCounter() > 5) {
                    programsRepository.setInactive(String.valueOf(program.getId()));
                    buggyPrograms.add(program);
                }
                else {
                    programsRepository.increaseDeletionCounter(program.getId());
                }
            }
        }

        //Add programs with high memory
        for (Program program : programsWithHighMemory) {
            ProgramForDeletion DeletionCandidate = new ProgramForDeletion();
            DeletionCandidate.setProgramId(program.getId());
            DeletionCandidate.setPotentialDeletionReason(DeletionReason.MEMORY);
            programsRepository.addProgramForDeletion(DeletionCandidate);
        }

        //Add programs with high cpu
        for (Program program : programsWithHighCPU) {
            ProgramForDeletion DeletionCandidate = new ProgramForDeletion();
            DeletionCandidate.setProgramId(program.getId());
            DeletionCandidate.setPotentialDeletionReason(DeletionReason.CPU);
            programsRepository.addProgramForDeletion(DeletionCandidate);
        }

        //Add programs with high cpu
        for (Program program : buggyPrograms) {
            ProgramForDeletion DeletionCandidate = new ProgramForDeletion();
            DeletionCandidate.setProgramId(program.getId());
            DeletionCandidate.setPotentialDeletionReason(DeletionReason.BUGGY);
            programsRepository.addProgramForDeletion(DeletionCandidate);
        }

        //Add malicious programs
        for (Program program : maliciousPrograms) {
            ProgramForDeletion DeletionCandidate = new ProgramForDeletion();
            DeletionCandidate.setProgramId(program.getId());
            DeletionCandidate.setPotentialDeletionReason(DeletionReason.MALICIOUS);
            programsRepository.addProgramForDeletion(DeletionCandidate);
        }
    }

    private void stopTheWorld() {
        System.out.println("Stopping the world");
    }


    private void tryToLogin(String requestString, String userName, String userSecret) throws IllegalAccessException {
        logger.info("performing login for deactivation request: " +
                requestString);
        LoginResponse loginResponse = loginService.login(userName,
                userSecret);
        if (loginResponse == null) {
            throw new IllegalAccessException("login failed");
        }
        logger.info("logged in successfully");
    }

    private void validateDR(DRequest dRequest) {
        if ((dRequest.getEmail() == null
                || dRequest.getEmail().isEmpty())
                && (null == dRequest.getUserName() ||
                dRequest.getUserName().isEmpty())) {
            throw new IllegalArgumentException("Both email and user name are empty");
        }
        if (null == dRequest.getUserSecret()
                || dRequest.getUserSecret().isEmpty()) {
            throw new IllegalArgumentException("Password is empty");
        }
        if (null == dRequest.getProgramId() && null == dRequest.getIds()) {
            throw new IllegalArgumentException("identifiers are not valid");
        }
    }

    private void validateId(int programId) {
        if (programId <= 0) {
            throw new IllegalArgumentException("Program id must be positive");
        }
    }


    private void validateProgram(Program program) {
        if (program.getName() == null || program.getName().isEmpty()) {
            throw new ValidationException("The Name cannot be empty");
        }
        if (program.getCreator() == null || program.getCreator().isEmpty()) {
            throw new ValidationException("The creator cannot be empty");
        }
        if (program.getPurpose() == null || program.getPurpose().isEmpty()) {
            throw new ValidationException("The purpose cannot be empty");
        }
        if (program.getCpuUsage() > 30) {
            throw new ValidationException("cpu is over 30");
        }

        if (program.getMemoryConsumption() > 30) {
            throw new ValidationException("memory is over 30");
        }
    }
}
