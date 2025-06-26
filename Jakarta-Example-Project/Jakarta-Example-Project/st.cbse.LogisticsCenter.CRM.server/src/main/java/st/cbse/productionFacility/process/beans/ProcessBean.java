package st.cbse.productionFacility.process.beans;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
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
import st.cbse.productionFacility.production.data.Transport.TransportStatus;
import st.cbse.productionFacility.production.interfaces.IProductionMgmt;
import st.cbse.productionFacility.production.machine.dto.MachineDTO;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;

@Stateless
public class ProcessBean implements IProcessMgmt {
    
    private static final Logger LOG = Logger.getLogger(ProcessBean.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private IMachineMgmt machineMgmt;
    
    @EJB
    private IProductionMgmt productionMgmt;
    
    @Resource
    private TimerService timerService;
    
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
        LOG.info("Attempting to start process automatically...");
        tryToStartProcess(process);
        LOG.info("========================================");
        
        return process.getId();
    }
    
    private void tryToStartProcess(Process process) {
        LOG.info("Checking if process can be started: " + process.getId());
        
        ProcessStep firstStep = process.getCurrentStep();
        if (firstStep == null) {
            LOG.warning("No first step found for process " + process.getId());
            return;
        }
        
        LOG.info("First step type: " + firstStep.getStepType());
        
        String machineType = mapStepToMachineType(firstStep.getStepType());
        LOG.info("Looking for available machines of type: " + machineType);
        
        List<MachineDTO> availableMachines = machineMgmt.findAvailableMachineDTOsByType(machineType);
        LOG.info("Found " + availableMachines.size() + " available machines");
        
        if (!availableMachines.isEmpty()) {
            MachineDTO machineDTO = availableMachines.get(0);
            LOG.info("Found available machine: " + machineDTO.getId() + " (Type: " + machineDTO.getType() + ")");
            
            boolean reserved = machineMgmt.reserveMachine(machineDTO.getId(), process.getId());
            if (reserved) {
                LOG.info("Machine " + machineDTO.getId() + " reserved successfully");
                
                firstStep.setAssignedMachineId(machineDTO.getId());
                
                process.setStatus(ProcessStatus.IN_PROGRESS);
                em.merge(process);
                
                LOG.info("Process " + process.getId() + " started with machine " + machineDTO.getId());
                
                if (machineMgmt.programMachine(machineDTO.getId())) {
                    LOG.info("Machine " + machineDTO.getId() + " programmed successfully");
                    
                    LOG.info("Executing machine " + machineDTO.getId() + " for process " + process.getId());
                    boolean executed = machineMgmt.executeMachine(machineDTO.getId(), process.getId());
                    
                    if (executed) {
                        LOG.info("Machine " + machineDTO.getId() + " executed successfully!");
                        
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

    private void tryToProcessNextStep(Process process, ProcessStep nextStep) {
        LOG.info("Attempting to process next step: " + nextStep.getStepType() + " for process " + process.getId());
        
        if (process.getStatus() == ProcessStatus.PAUSED) {
            LOG.info("Process " + process.getId() + " is paused - skipping next step processing");
            return;
        }

        ProcessStep previousStep = getPreviousCompletedStep(process, nextStep);
        
        if (previousStep != null && previousStep.getAssignedMachineId() != null) {
            LOG.info("Previous step found: " + previousStep.getStepType() + 
                    " with machine " + previousStep.getAssignedMachineId());
            
            MachineDTO fromMachineDTO = machineMgmt.getMachineDTO(previousStep.getAssignedMachineId());
            
            if (fromMachineDTO == null) {
                LOG.warning("Cannot get DTO for machine " + previousStep.getAssignedMachineId());
                return;
            }
            
            LOG.info("Machine " + fromMachineDTO.getId() + " state: " +
                    " Status=" + fromMachineDTO.getStatus() + 
                    ", OutputProcessId=" + fromMachineDTO.getOutputProcessId() + 
                    ", HasOutput=" + fromMachineDTO.isHasOutput());
            
            UUID itemId = fromMachineDTO.getOutputProcessId();
            
            if (itemId == null) {
                LOG.info("Item not yet ready in output of machine " + previousStep.getAssignedMachineId() + 
                        " - scheduling retry in 2 seconds");
                
                timerService.createSingleActionTimer(2000, new TimerConfig(
                    new ProcessingTask(process.getId(), ProcessingTask.Type.NEXT_STEP), false));
                return;
            }
            
            LOG.info("Output is ready with itemId: " + itemId + " - proceeding with transport setup");
            
            String machineType = mapStepToMachineType(nextStep.getStepType());
            List<MachineDTO> availableMachines = machineMgmt.findAvailableMachineDTOsByType(machineType);
            
            if (!availableMachines.isEmpty()) {
                MachineDTO targetMachine = availableMachines.get(0);
                
                if (machineMgmt.reserveMachine(targetMachine.getId(), process.getId())) {
                    nextStep.setAssignedMachineId(targetMachine.getId());
                    em.merge(process);
                    
                    boolean transported = productionMgmt.transportItem(
                    	    itemId,
                    	    process.getId(),
                    	    previousStep.getAssignedMachineId(),
                    	    targetMachine.getId()
                    	);
                    
                    if (transported) {
                        LOG.info("Transport initiated successfully");
                    } else {
                        LOG.warning("Transport failed");
                        machineMgmt.stopMachine(targetMachine.getId());
                        nextStep.setAssignedMachineId(null);
                        em.merge(process);
                        
                        timerService.createSingleActionTimer(5000, new TimerConfig(
                            new ProcessingTask(process.getId(), ProcessingTask.Type.NEXT_STEP), false));
                    }
                } else {
                    LOG.warning("Failed to reserve target machine");
                    timerService.createSingleActionTimer(5000, new TimerConfig(
                        new ProcessingTask(process.getId(), ProcessingTask.Type.NEXT_STEP), false));
                }
            } else {
                LOG.info("No available machine for step " + nextStep.getStepType());
                timerService.createSingleActionTimer(10000, new TimerConfig(
                    new ProcessingTask(process.getId(), ProcessingTask.Type.NEXT_STEP), false));
            }
        } else {
            LOG.info("No previous step - starting step directly");
            tryToStartStep(process, nextStep);
        }
    }
    
    private void queueProcess(Process process) {
        process.setStatus(ProcessStatus.QUEUED);
        em.merge(process);
        LOG.info("Process " + process.getId() + " added to queue");
    }
    
    private String mapStepToMachineType(String stepType) {
        switch (stepType.toUpperCase()) {
            case "PRINTING":
                return "PrintingMachine";
            case "PAINT":
                return "PaintMachine";
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
            case "PAINTJOB":
                LOG.fine("Mapped to PAINT");
                return "PAINT";
            case "SMOOTHING":
                LOG.fine("Mapped to SMOOTHING");
                return "SMOOTHING";
            case "ENGRAVING":
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
    public boolean pauseProcess(UUID processId) {
        LOG.info("Pausing process: " + processId);
        Process process = em.find(Process.class, processId);
        
        if (process == null) {
            LOG.warning("Process not found: " + processId);
            return false;
        }
        
        if (process.getStatus() != ProcessStatus.IN_PROGRESS && 
            process.getStatus() != ProcessStatus.QUEUED) {
            LOG.warning("Cannot pause process in status: " + process.getStatus());
            return false;
        }
        
        ProcessStatus oldStatus = process.getStatus();
        process.setStatus(ProcessStatus.PAUSED);
        em.merge(process);
        
        for (ProcessStep step : process.getSteps()) {
            if (step.getAssignedMachineId() != null && !step.isCompleted()) {
                pauseMachineForProcess(step.getAssignedMachineId(), processId);
            }
        }
        
        LOG.info("Process " + processId + " paused successfully (was " + oldStatus + ")");
        return true;
    }

    @Override
    public boolean resumeProcess(UUID processId) {
        LOG.info("Resuming process: " + processId);
        Process process = em.find(Process.class, processId);
        
        if (process == null) {
            LOG.warning("Process not found: " + processId);
            return false;
        }
        
        if (process.getStatus() != ProcessStatus.PAUSED) {
            LOG.warning("Cannot resume process not in PAUSED status: " + process.getStatus());
            return false;
        }
        
        process.setStatus(ProcessStatus.IN_PROGRESS);
        em.merge(process);
        
        ProcessStep currentStep = process.getCurrentStep();
        if (currentStep != null) {
            if (currentStep.getAssignedMachineId() != null) {
                resumeMachineForProcess(currentStep.getAssignedMachineId(), processId);
            } else {
                scheduleNextStepProcessing(processId);
            }
        }
        
        LOG.info("Process " + processId + " resumed successfully");
        return true;
    }

    private void pauseMachineForProcess(UUID machineId, UUID processId) {
        machineMgmt.pauseMachine(machineId);
    }

    private void resumeMachineForProcess(UUID machineId, UUID processId) {
        machineMgmt.resumeMachine(machineId, processId);
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
            em.merge(process);
            
            scheduleNextStepProcessing(process.getId());
            
        } else {
            if (!"PACKAGING".equals(currentStep.getStepType()) || 
                !isTransportToStorageInProgress(processId)) {
                LOG.info("All steps completed - scheduling delivery to storage");
                scheduleStorageDelivery(process.getId(), currentStep.getAssignedMachineId());
            } else {
                LOG.info("All steps completed - storage delivery already in progress");
                process.setStatus(ProcessStatus.COMPLETED);
                em.merge(process);
            }
        }
        
        return true;
    }

    private boolean isTransportToStorageInProgress(UUID processId) {
        Long count = em.createQuery(
            "SELECT COUNT(t) FROM Transport t WHERE t.processId = :processId " +
            "AND t.status = :status AND t.toMachineId IS NULL", Long.class)
            .setParameter("processId", processId)
            .setParameter("status", TransportStatus.IN_TRANSIT)
            .getSingleResult();
        
        return count > 0;
    }

    private void scheduleNextStepProcessing(UUID processId) {
        timerService.createSingleActionTimer(1000, new TimerConfig(
            new ProcessingTask(processId, ProcessingTask.Type.NEXT_STEP), false));
    }

    private void scheduleStorageDelivery(UUID processId, UUID lastMachineId) {
        timerService.createSingleActionTimer(1000, new TimerConfig(
            new ProcessingTask(processId, lastMachineId, ProcessingTask.Type.STORAGE_DELIVERY), false));
    }

    @Timeout
    public void handleProcessingTimer(Timer timer) {
        ProcessingTask task = (ProcessingTask) timer.getInfo();
        if (task == null) return;
        
        Process process = em.find(Process.class, task.getProcessId());
        if (process == null) return;
        
        switch (task.getType()) {
            case NEXT_STEP:
                ProcessStep nextStep = process.getCurrentStep();
                if (nextStep != null) {
                    tryToProcessNextStep(process, nextStep);
                }
                break;
                
            case STORAGE_DELIVERY:
                boolean delivered = productionMgmt.deliverToStorage(
                    task.getProcessId(),
                    task.getProcessId(),
                    task.getMachineId()
                );
                
                if (delivered) {
                    process.setStatus(ProcessStatus.COMPLETED);
                    em.merge(process);
                    LOG.info("Process " + task.getProcessId() + " completed and delivered to storage!");
                }
                break;
        }
    }

    private static class ProcessingTask implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		enum Type { NEXT_STEP, STORAGE_DELIVERY }
        
        private final UUID processId;
        private final UUID machineId;
        private final Type type;
        
        ProcessingTask(UUID processId, Type type) {
            this.processId = processId;
            this.machineId = null;
            this.type = type;
        }
        
        ProcessingTask(UUID processId, UUID machineId, Type type) {
            this.processId = processId;
            this.machineId = machineId;
            this.type = type;
        }

		public UUID getProcessId() {
			return processId;
		}

		public UUID getMachineId() {
			return machineId;
		}

		public Type getType() {
			return type;
		}
        
        
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
    
    private ProcessStep getPreviousCompletedStep(Process process, ProcessStep currentStep) {
        List<ProcessStep> steps = process.getSteps();
        int currentIndex = steps.indexOf(currentStep);
        
        if (currentIndex > 0) {
            for (int i = currentIndex - 1; i >= 0; i--) {
                ProcessStep step = steps.get(i);
                if (step.isCompleted() && step.getAssignedMachineId() != null) {
                    return step;
                }
            }
        }
        return null;
    }

    private void tryToStartStep(Process process, ProcessStep step) {
        String machineType = mapStepToMachineType(step.getStepType());
        List<MachineDTO> availableMachines = machineMgmt.findAvailableMachineDTOsByType(machineType);
        
        if (!availableMachines.isEmpty()) {
            MachineDTO machineDTO = availableMachines.get(0);
            
            if (machineMgmt.reserveMachine(machineDTO.getId(), process.getId())) {
                step.setAssignedMachineId(machineDTO.getId());
                em.merge(process);
                
                if (machineMgmt.programMachine(machineDTO.getId())) {
                	machineMgmt.executeMachine(machineDTO.getId(), process.getId());;
                }
            }
        }
    }
}