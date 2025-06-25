package st.cbse.productionFacility.process.beans;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

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

@Stateless
public class ProcessBean implements IProcessMgmt {
    
    private static final Logger LOG = Logger.getLogger(ProcessBean.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
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
        LOG.info("========================================");
        
        return process.getId();
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