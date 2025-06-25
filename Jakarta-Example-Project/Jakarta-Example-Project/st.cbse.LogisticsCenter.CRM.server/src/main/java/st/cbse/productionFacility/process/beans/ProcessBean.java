package st.cbse.productionFacility.process.beans;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import st.cbse.crm.dto.OptionDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.productionFacility.process.data.Process;
import st.cbse.productionFacility.process.data.ProcessStep;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;
import st.cbse.productionFacility.process.dto.ProcessDTO;
import st.cbse.productionFacility.process.dto.ProcessMapper;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;
import st.cbse.productionFacility.production.machine.dto.MachineDTO;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;

@Stateless
public class ProcessBean implements IProcessMgmt {
    
    private static final Logger LOG = Logger.getLogger(ProcessBean.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private IMachineMgmt machineMgmt;
    
    @Override
    public UUID createProcessFromPrintRequest(PrintRequestDTO printRequest) {
        LOG.info("========================================");
        LOG.info("Creating new process from PrintRequest");
        LOG.info("PrintRequest ID: " + printRequest.getId());
        LOG.info("STL Path: " + printRequest.getStlPath());
        LOG.info("Note: " + printRequest.getNote());
        LOG.info("Total Price: €" + printRequest.getPrice());
        LOG.info("Number of options: " + printRequest.getOptions().size());
        
        Process process = new Process(printRequest.getId());
        LOG.info("New Process ID: " + process.getId());
        
        int stepOrder = 0;
        process.addStep(new ProcessStep("PRINTING", stepOrder++));
        LOG.info("Added step " + (stepOrder-1) + ": PRINTING");
        
        for (OptionDTO option : printRequest.getOptions()) {
            LOG.info("Processing option: " + option.getType() + 
                    " (Type: " + option.getClass().getSimpleName() + ", Price: €" + option.getPrice() + ")");
            
            String stepType = mapOptionToStepType(option);
            if (stepType != null) {
                process.addStep(new ProcessStep(stepType, stepOrder++));
                LOG.info("Added step " + (stepOrder-1) + ": " + stepType);
            } else {
                LOG.warning("No step mapping found for option: " + option.getType());
            }
        }
        
        process.addStep(new ProcessStep("PACKAGING", stepOrder));
        LOG.info("Added step " + stepOrder + ": PACKAGING");
        
        process.setStatus(ProcessStatus.CREATED);
        em.persist(process);
        
        LOG.info("Process successfully created and persisted");
        LOG.info("Total steps in process: " + process.getSteps().size());
        LOG.info("Process status: " + process.getStatus());
        LOG.info("Created process " + process.getId() + " with " + process.getSteps().size() + " steps");
        
        // Essayer de démarrer le process automatiquement
        LOG.info("Attempting to start process automatically...");
        tryToStartProcess(process);
        
        LOG.info("========================================");
        
        return process.getId();
    }
    
    private void tryToStartProcess(Process process) {
        LOG.info("Checking if process can be started: " + process.getId());
        
        // Récupérer la première étape
        ProcessStep firstStep = process.getCurrentStep();
        if (firstStep == null) {
            LOG.warning("No first step found for process " + process.getId());
            return;
        }
        
        LOG.info("First step type: " + firstStep.getStepType());
        
        // Mapper le type d'étape au type de machine
        String machineType = mapStepToMachineType(firstStep.getStepType());
        LOG.info("Looking for available machines of type: " + machineType);
        
        // Si IMachineMgmt a une méthode qui retourne directement des DTOs
        List<MachineDTO> availableMachines = machineMgmt.findAvailableMachineDTOsByType(machineType);
        LOG.info("Found " + availableMachines.size() + " available machines");
        
        if (!availableMachines.isEmpty()) {
            MachineDTO machineDTO = availableMachines.get(0);
            LOG.info("Found available machine: " + machineDTO.getId() + " (Type: " + machineDTO.getType() + ")");
            
            // Réserver la machine pour ce process
            boolean reserved = machineMgmt.reserveMachine(machineDTO.getId(), process.getId());
            if (reserved) {
                LOG.info("Machine " + machineDTO.getId() + " reserved successfully");
                
                // Assigner la machine à l'étape
                firstStep.setAssignedMachineId(machineDTO.getId());
                
                // Mettre à jour le statut du process
                process.setStatus(ProcessStatus.IN_PROGRESS);
                em.merge(process);
                
                LOG.info("Process " + process.getId() + " started with machine " + machineDTO.getId());
                
                // Programmer et exécuter la machine
                if (machineMgmt.programMachine(machineDTO.getId())) {
                    LOG.info("Machine " + machineDTO.getId() + " programmed successfully");
                    
                    // NOUVEAU : Exécuter la machine immédiatement
                    LOG.info("Executing machine " + machineDTO.getId() + " for process " + process.getId());
                    boolean executed = machineMgmt.executeMachine(machineDTO.getId());
                    
                    if (executed) {
                        LOG.info("Machine " + machineDTO.getId() + " executed successfully!");
                        
                        // Vérifier si on peut passer à l'étape suivante
                        ProcessStep nextStep = process.getCurrentStep();
                        if (nextStep != null && !nextStep.equals(firstStep)) {
                            LOG.info("Moving to next step: " + nextStep.getStepType());
                            tryToProcessNextStep(process, nextStep);
                        } else if (process.getStatus() == ProcessStatus.COMPLETED) {
                            LOG.info("Process " + process.getId() + " completed successfully!");
                        }
                    } else {
                        LOG.warning("Failed to execute machine " + machineDTO.getId());
                    }
                }
            } else {
                LOG.warning("Failed to reserve machine " + machineDTO.getId());
                queueProcess(process);
            }
        } else {
            LOG.info("No available machines found - queueing process");
            queueProcess(process);
        }
    }

    // Nouvelle méthode pour traiter l'étape suivante
    private void tryToProcessNextStep(Process process, ProcessStep nextStep) {
        LOG.info("Attempting to process next step: " + nextStep.getStepType() + " for process " + process.getId());
        
        String machineType = mapStepToMachineType(nextStep.getStepType());
        List<MachineDTO> availableMachines = machineMgmt.findAvailableMachineDTOsByType(machineType);
        
        if (!availableMachines.isEmpty()) {
            MachineDTO machineDTO = availableMachines.get(0);
            
            if (machineMgmt.reserveMachine(machineDTO.getId(), process.getId())) {
                nextStep.setAssignedMachineId(machineDTO.getId());
                em.merge(process);
                
                if (machineMgmt.programMachine(machineDTO.getId())) {
                    boolean executed = machineMgmt.executeMachine(machineDTO.getId());
                    
                    if (executed) {
                        LOG.info("Step " + nextStep.getStepType() + " executed successfully");
                        
                        // Récursivement traiter l'étape suivante
                        ProcessStep followingStep = process.getCurrentStep();
                        if (followingStep != null && !followingStep.equals(nextStep)) {
                            tryToProcessNextStep(process, followingStep);
                        }
                    }
                }
            }
        } else {
            LOG.info("No available machine for step " + nextStep.getStepType() + " - process will wait");
        }
    }
    
    private void queueProcess(Process process) {
        process.setStatus(ProcessStatus.QUEUED);
        em.merge(process);
        LOG.info("Process " + process.getId() + " added to queue");
    }
    
    private String mapStepToMachineType(String stepType) {
        // Mapper les types d'étapes aux types de machines
        // À adapter selon votre modèle de données
        switch (stepType.toUpperCase()) {
            case "PRINTING":
                return "PrintingMachine";
            case "PAINT":
                return "PaintingMachine";
            case "SMOOTHING":
                return "SmoothingMachine";
            case "ENGRAVING":
                return "EngravingMachine";
            case "PACKAGING":
                return "PackagingMachine";
            default:
                LOG.warning("Unknown step type for machine mapping: " + stepType);
                return stepType + "Machine";
        }
    }
    
    private String mapOptionToStepType(OptionDTO option) {
        String optionType = option.getType().toUpperCase();
        LOG.fine("Mapping option type: " + optionType);
        
        switch (optionType) {
            case "PaintJob":
                LOG.fine("Mapped to PAINT");
                return "PAINT";
            case "Smoothing":
                LOG.fine("Mapped to SMOOTHING");
                return "SMOOTHING";
            case "Engraving":
                LOG.fine("Mapped to ENGRAVING");
                return "ENGRAVING";
            default:
                LOG.warning("Unknown option type: " + optionType);
                return null;
        }
    }
    
    @Override
    public ProcessDTO getProcess(UUID processId) {
        LOG.info("Retrieving process: " + processId);
        Process process = em.find(Process.class, processId);
        
        if (process == null) {
            LOG.warning("Process not found: " + processId);
        } else {
            LOG.info("Process found - Status: " + process.getStatus());
        }
        
        return ProcessMapper.toDTO(process);
    }
    
    @Override
    public List<ProcessDTO> getAllProcesses() {
        LOG.info("Retrieving all processes");
        List<Process> processes = em.createQuery("SELECT p FROM Process p", Process.class)
                .getResultList();
        LOG.info("Found " + processes.size() + " processes");
        return ProcessMapper.toDTOList(processes);
    }
    
    @Override
    public List<ProcessDTO> getProcessesByStatus(String status) {
        LOG.info("Retrieving processes with status: " + status);
        List<Process> processes = em.createQuery(
                "SELECT p FROM Process p WHERE p.status = :status", Process.class)
                .setParameter("status", ProcessStatus.valueOf(status))
                .getResultList();
        LOG.info("Found " + processes.size() + " processes with status " + status);
        return ProcessMapper.toDTOList(processes);
    }
    
    @Override
    public boolean startProcess(UUID processId) {
        LOG.info("Starting process: " + processId);
        Process process = em.find(Process.class, processId);
        if (process == null || process.getStatus() != ProcessStatus.CREATED) {
            LOG.warning("Cannot start process - not found or invalid status");
            return false;
        }
        
        process.setStatus(ProcessStatus.QUEUED);
        em.merge(process);
        LOG.info("Process " + processId + " started successfully - Status: QUEUED");
        return true;
    }
    
    @Override
    public boolean cancelProcess(UUID processId) {
        LOG.info("Cancelling process: " + processId);
        Process process = em.find(Process.class, processId);
        if (process == null || process.getStatus() == ProcessStatus.COMPLETED) {
            LOG.warning("Cannot cancel process - not found or already completed");
            return false;
        }
        
        process.setStatus(ProcessStatus.CANCELLED);
        em.merge(process);
        LOG.info("Process " + processId + " cancelled");
        return true;
    }
    
    @Override
    public boolean validateCurrentStep(UUID processId, UUID machineId) {
        LOG.info("Validating step for process: " + processId + " by machine: " + machineId);
        Process process = em.find(Process.class, processId);
        if (process == null) return false;
        
        ProcessStep currentStep = process.getCurrentStep();
        if (currentStep == null || !machineId.equals(currentStep.getAssignedMachineId())) {
            LOG.warning("Validation failed - invalid step or machine");
            return false;
        }
        
        currentStep.setCompleted(true);
        LOG.info("Step " + currentStep.getStepType() + " marked as completed");
        
        if (process.moveToNextStep()) {
            process.setStatus(ProcessStatus.IN_PROGRESS);
            LOG.info("Moved to next step");
        } else {
            process.setStatus(ProcessStatus.COMPLETED);
            LOG.info("All steps completed - Process finished!");
        }
        
        em.merge(process);
        LOG.info("Process " + processId + " step " + currentStep.getStepType() + " completed");
        
        return true;
    }
    
    @Override
    public void notifyStepCompleted(UUID processId, UUID machineId) {
        LOG.info("Step completion notification - Process: " + processId + ", Machine: " + machineId);
        validateCurrentStep(processId, machineId);
    }
    
    @Override
    public void notifyMachineStopped(UUID processId) {
        LOG.warning("Machine stopped for process: " + processId);
        Process process = em.find(Process.class, processId);
        if (process != null) {
            process.setStatus(ProcessStatus.FAILED);
            em.merge(process);
            LOG.warning("Process " + processId + " failed due to machine stop");
        }
    }
    
    @Override
    public ProcessDTO getCurrentStepInfo(UUID processId) {
        LOG.info("Getting current step info for process: " + processId);
        return getProcess(processId);
    }
    
    @Override
    public boolean isProcessComplete(UUID processId) {
        Process process = em.find(Process.class, processId);
        boolean complete = process != null && process.getStatus() == ProcessStatus.COMPLETED;
        LOG.info("Process " + processId + " complete: " + complete);
        return complete;
    }
}