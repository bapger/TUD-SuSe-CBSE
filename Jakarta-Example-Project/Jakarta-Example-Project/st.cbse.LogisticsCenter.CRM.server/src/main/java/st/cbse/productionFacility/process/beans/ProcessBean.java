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
        Process process = new Process(printRequest.getId());
        
        int stepOrder = 0;
        process.addStep(new ProcessStep("PRINTING", stepOrder++));
        
        for (OptionDTO option : printRequest.getOptions()) {
            String stepType = mapOptionToStepType(option);
            if (stepType != null) {
                process.addStep(new ProcessStep(stepType, stepOrder++));
            }
        }
        
        process.addStep(new ProcessStep("PACKAGING", stepOrder));
        
        process.setStatus(ProcessStatus.CREATED);
        em.persist(process);
        
        LOG.info("Created process " + process.getId() + " with " + process.getSteps().size() + " steps");
        return process.getId();
    }
    
    private String mapOptionToStepType(OptionDTO option) {
        String optionType = option.getType().toUpperCase();
        switch (optionType) {
            case "PAINT":
            case "PAINTING":
                return "PAINT";
            case "SMOOTH":
            case "SMOOTHING":
                return "SMOOTHING";
            case "ENGRAVE":
            case "ENGRAVING":
                return "ENGRAVING";
            default:
                return null;
        }
    }
    
    @Override
    public ProcessDTO getProcess(UUID processId) {
        Process process = em.find(Process.class, processId);
        return ProcessMapper.toDTO(process);
    }
    
    @Override
    public List<ProcessDTO> getAllProcesses() {
        List<Process> processes = em.createQuery("SELECT p FROM Process p", Process.class)
                .getResultList();
        return ProcessMapper.toDTOList(processes);
    }
    
    @Override
    public List<ProcessDTO> getProcessesByStatus(String status) {
        List<Process> processes = em.createQuery(
                "SELECT p FROM Process p WHERE p.status = :status", Process.class)
                .setParameter("status", ProcessStatus.valueOf(status))
                .getResultList();
        return ProcessMapper.toDTOList(processes);
    }
    
    @Override
    public boolean startProcess(UUID processId) {
        Process process = em.find(Process.class, processId);
        if (process == null || process.getStatus() != ProcessStatus.CREATED) {
            return false;
        }
        
        process.setStatus(ProcessStatus.QUEUED);
        em.merge(process);
        return true;
    }
    
    @Override
    public boolean cancelProcess(UUID processId) {
        Process process = em.find(Process.class, processId);
        if (process == null || process.getStatus() == ProcessStatus.COMPLETED) {
            return false;
        }
        
        process.setStatus(ProcessStatus.CANCELLED);
        em.merge(process);
        return true;
    }
    
    @Override
    public boolean validateCurrentStep(UUID processId, UUID machineId) {
        Process process = em.find(Process.class, processId);
        if (process == null) return false;
        
        ProcessStep currentStep = process.getCurrentStep();
        if (currentStep == null || !machineId.equals(currentStep.getAssignedMachineId())) {
            return false;
        }
        
        currentStep.setCompleted(true);
        
        if (process.moveToNextStep()) {
            process.setStatus(ProcessStatus.IN_PROGRESS);
        } else {
            process.setStatus(ProcessStatus.COMPLETED);
        }
        
        em.merge(process);
        LOG.info("Process " + processId + " step " + currentStep.getStepType() + " completed");
        
        return true;
    }
    
    @Override
    public void notifyStepCompleted(UUID processId, UUID machineId) {
        validateCurrentStep(processId, machineId);
    }
    
    @Override
    public void notifyMachineStopped(UUID processId) {
        Process process = em.find(Process.class, processId);
        if (process != null) {
            process.setStatus(ProcessStatus.FAILED);
            em.merge(process);
            LOG.warning("Process " + processId + " failed due to machine stop");
        }
    }
    
    @Override
    public ProcessDTO getCurrentStepInfo(UUID processId) {
        return getProcess(processId);
    }
    
    @Override
    public boolean isProcessComplete(UUID processId) {
        Process process = em.find(Process.class, processId);
        return process != null && process.getStatus() == ProcessStatus.COMPLETED;
    }
}