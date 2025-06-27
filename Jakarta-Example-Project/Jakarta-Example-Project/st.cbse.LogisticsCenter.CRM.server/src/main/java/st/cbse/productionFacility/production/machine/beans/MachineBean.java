package st.cbse.productionFacility.production.machine.beans;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;
import st.cbse.productionFacility.production.machine.dto.MachineDTO;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;
import st.cbse.productionFacility.process.dto.ProcessDTO;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;

@Stateless
public class MachineBean implements IMachineMgmt {

	private static final Logger LOG = Logger.getLogger(MachineBean.class.getName());

	@PersistenceContext
	private EntityManager em;

	@EJB
	private IProcessMgmt processMgmt;

	@EJB
	private IStorageMgmt storageMgmt;
	
	@Resource
	private TimerService timerService;

	@Override
	public MachineDTO getMachineDTO(UUID machineId) {
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null) {
	        return null;
	    }
	    
	    em.refresh(machine);
	    
	    return convertToDTO(machine);
	}

	@Override
	public boolean pauseMachine(UUID machineId) {
	    LOG.info("Pausing machine: " + machineId);
	    Machine machine = em.find(Machine.class, machineId);
	    
	    if (machine == null) {
	        LOG.warning("Machine not found: " + machineId);
	        return false;
	    }
	    
	    if (machine.getStatus() == MachineStatus.ACTIVE) {
	        machine.setStatus(MachineStatus.PAUSED);
	        em.merge(machine);
	        LOG.info("Machine " + machineId + " paused while active");
	    } else if (machine.getStatus() == MachineStatus.RESERVED) {
	        machine.setStatus(MachineStatus.PAUSED);
	        em.merge(machine);
	        LOG.info("Machine " + machineId + " marked as paused (was reserved)");
	    }
	    
	    return true;
	}

	@Override
	public boolean resumeMachine(UUID machineId, UUID processId) {
	    LOG.info("Resuming machine: " + machineId + " for process: " + processId);
	    Machine machine = em.find(Machine.class, machineId);
	    
	    if (machine == null) {
	        LOG.warning("Machine not found: " + machineId);
	        return false;
	    }
	    
	    if (machine.getStatus() != MachineStatus.PAUSED) {
	        LOG.warning("Machine not paused: " + machineId);
	        return false;
	    }
	    
	    if (machine.getActiveProcessId() != null) {
	        return executeMachine(machineId, processId);
	    } else {
	        machine.setStatus(MachineStatus.RESERVED);
	        em.merge(machine);
	    }
	    
	    LOG.info("Machine " + machineId + " resumed");
	    return true;
	}

	@Override
	public boolean isMachinePausedForProcess(UUID machineId, UUID processId) {
	    Machine machine = em.find(Machine.class, machineId);
	    return machine != null && machine.getStatus() == MachineStatus.PAUSED &&
	           (machine.getActiveProcessId() != null && machine.getActiveProcessId().equals(processId));
	}
	
	@Override
	public boolean executeMachine(UUID machineId, UUID processId) {
	    ProcessDTO processDTO = processMgmt.getProcess(processId);
	    if (processDTO != null && "PAUSED".equals(processDTO.getStatus())) {
	        LOG.warning("Cannot execute machine for paused process: " + processId);
	        Machine machine = em.find(Machine.class, machineId);
	        if (machine != null) {
	            machine.setStatus(MachineStatus.PAUSED);
	            em.merge(machine);
	        }
	        return false;
	    }

	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null || (machine.getStatus() != MachineStatus.RESERVED && machine.getStatus() != MachineStatus.PAUSED)) {
	        LOG.warning("Cannot execute machine " + machineId + " - machine null or not in correct state");
	        return false;
	    }
	    
	    if (machine.getActiveProcessId() != null && machine.getStatus() != MachineStatus.PAUSED) {
	        LOG.warning("Machine " + machineId + " already has active process: " + machine.getActiveProcessId());
	        return false;
	    }

	    LOG.info("Machine " + machineId + " before processing - outputProcessId: " + machine.getOutputProcessId());
	    
	    UUID itemId = null;
	    
	    if (machine.getClass().getSimpleName().equals("PrintingMachine") && machine.getActiveProcessId() == null) {
	        LOG.info("PrintingMachine detected - creating UnfinishedProduct");
	        if (processDTO != null) {
	            itemId = storageMgmt.createUnfinishedProduct(
	                processId,
	                processDTO.getPrintRequestId(),
	                "stl_path"
	            );
	            LOG.info("Created UnfinishedProduct with itemId: " + itemId + " for process: " + processId);
	        }
	    }
	    
	    if (machine.getStatus() == MachineStatus.PAUSED && machine.getActiveProcessId() != null) {
	        LOG.info("Resuming machine " + machineId + " processing");
	        machine.setStatus(MachineStatus.ACTIVE);
	    } else {
	        if (!machine.hasInput()) {
	            machine.setActiveProcessId(processId);
	            machine.setStatus(MachineStatus.ACTIVE);
	        } else {
	            boolean started = machine.startProcessing();
	            if (!started) {
	                LOG.warning("Failed to start processing on machine " + machineId);
	                return false;
	            }
	        }
	    }
	    
	    em.merge(machine);
	    em.flush();
	    
	    LOG.info("Machine " + machineId + " processing - " + machine.getActionMessage());
	    
	    long processingTime = machine.getProcessingTimeMillis();
	    timerService.createSingleActionTimer(processingTime, new TimerConfig(
	        new MachineProcessingInfo(processId, machineId, itemId), false));
	    
	    return true;
	}

	@Timeout
	public void handleMachineTimer(Timer timer) {
	    Object info = timer.getInfo();
	    
	    if (info instanceof MachineProcessingInfo) {
	        MachineProcessingInfo processingInfo = (MachineProcessingInfo) info;
	        finishMachineProcessing(processingInfo.getMachineId(), processingInfo.getProcessId(), processingInfo.getItemId());
	    } else if (info instanceof MachineCompletionInfo) {
	        MachineCompletionInfo completionInfo = (MachineCompletionInfo) info;
	        LOG.info("Notifying process " + completionInfo.getProcessId() + 
	                " that machine " + completionInfo.getMachineId() + " completed");
	        processMgmt.notifyStepCompleted(completionInfo.getProcessId(), completionInfo.getMachineId());
	    }
	}

	private void finishMachineProcessing(UUID machineId, UUID processId, UUID itemId) {
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null) {
	        LOG.warning("Machine not found: " + machineId);
	        return;
	    }
	    
	    ProcessDTO processDTO = processMgmt.getProcess(processId);
	    if (processDTO != null && "PAUSED".equals(processDTO.getStatus())) {
	        LOG.info("Process is paused - machine " + machineId + " will not finish processing");
	        machine.setStatus(MachineStatus.PAUSED);
	        em.merge(machine);
	        return;
	    }
	    
	    boolean finished = machine.finishProcessing();
	    if (!finished) {
	        LOG.warning("Failed to finish processing on machine " + machineId);
	        return;
	    }
	    
	    if (itemId != null) {
	        machine.setOutputProcessId(itemId);
	        LOG.info("Set outputProcessId to itemId: " + itemId);
	    }
	    
	    em.merge(machine);
	    em.flush();
	    
	    LOG.info("Machine " + machineId + " completed - outputProcessId: " + machine.getOutputProcessId());
	    
	    processMgmt.notifyStepCompleted(processId, machineId);
	}

	@Override
	public List<Machine> listAllMachines() {
		return em.createQuery("SELECT m FROM Machine m", Machine.class).getResultList();
	}

	@Override
	public List<Machine> findAvailableMachinesByType(String machineType) {
	    return em.createQuery(
	            "SELECT m FROM Machine m WHERE m.status = :status " +
	            "AND m.outputProcessId IS NULL " +
	            "AND TYPE(m) = :type", 
	            Machine.class)
	            .setParameter("status", MachineStatus.AVAILABLE)
	            .setParameter("type", machineType)
	            .getResultList();
	}

	@Override
	public Machine getMachine(UUID machineId) {
		return em.find(Machine.class, machineId);
	}

	@Override
	public boolean reserveMachine(UUID machineId, UUID processId) {
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null || machine.getStatus() != MachineStatus.AVAILABLE) {
	        LOG.warning("Cannot reserve machine " + machineId + " - not available or not found");
	        return false;
	    }
	    
	    if (machine.getActiveProcessId() != null || machine.getInputProcessId() != null) {
	        LOG.warning("Machine " + machineId + " has pending work - cannot reserve");
	        return false;
	    }
	    
	    if (machine.getOutputProcessId() != null) {
	        LOG.warning("Machine " + machineId + " has item in output - cannot reserve until output is cleared");
	        return false;
	    }
	    
	    machine.setStatus(MachineStatus.RESERVED);
	    em.merge(machine);
	    
	    LOG.info("Machine " + machineId + " reserved for process " + processId);
	    return true;
	}
	
	@Override
	public boolean programMachine(UUID machineId) {
		Machine machine = em.find(Machine.class, machineId);
		if (machine == null || machine.getStatus() != MachineStatus.RESERVED) {
			return false;
		}

		LOG.info("Machine " + machineId + " programmed");
		return true;
	}

	@Override
	public boolean stopMachine(UUID machineId) {
	    if (machineId == null) {
	        LOG.warning("Cannot stop machine with null ID");
	        return false;
	    }
	    
	    LOG.info("Attempting to stop machine " + machineId);
	    
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null) return false;
	    machine.setStatus(MachineStatus.AVAILABLE);
	    machine.setInputProcessId(null);
	    
	    em.merge(machine);
	    
	    LOG.info("Machine " + machineId + " stopped - output preserved: " + machine.getOutputProcessId());
	    return true;
	}

	@Override
	public UUID retrieveFromOutput(UUID machineId) {
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null || !machine.hasOutput()) {
	        return null;
	    }

	    UUID processId = machine.getOutputProcessId();
	    if (processId != null) {
	        machine.setOutputProcessId(null);
	        em.merge(machine);
	        LOG.info("Retrieved and cleared output " + processId + " from machine " + machineId);
	    }
	    return processId;
	}
	
	@Override
	public void clearOutput(UUID machineId) {
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine != null) {
	        LOG.info("Clearing output of machine " + machineId);
	        machine.setOutputProcessId(null);
	        em.merge(machine);
	    }
	}

	@Override
	public boolean canAcceptInput(UUID machineId) {
		Machine machine = em.find(Machine.class, machineId);

		if (machine == null) {
			LOG.warning("Machine " + machineId + " not found");
			return false;
		}

		LOG.info("Checking if machine " + machineId + " can accept input: " +
				"hasInput=" + machine.hasInput() + 
				", inputProcessId=" + machine.getInputProcessId() + 
				", status=" + machine.getStatus());

		boolean canAccept = machine.hasInput() && machine.getInputProcessId() == null &&
				(machine.getStatus() == MachineStatus.AVAILABLE || 
				machine.getStatus() == MachineStatus.RESERVED);

		LOG.info("Machine " + machineId + " can accept input: " + canAccept);

		return canAccept;
	}

	@Override
	public void notifyItemArrived(UUID machineId, UUID itemId) {
		Machine machine = em.find(Machine.class, machineId);
		if (machine != null && machine.hasInput()) {
			machine.setInputProcessId(itemId);
			em.merge(machine);
			LOG.info("Item " + itemId + " arrived at machine " + machineId);
		}
	}

	@Override
	public List<MachineDTO> findAvailableMachineDTOsByType(String machineType) {
	    List<Machine> allAvailableMachines = em.createQuery(
	            "SELECT m FROM Machine m WHERE m.status = :status " +
	            "AND m.outputProcessId IS NULL",
	            Machine.class)
	            .setParameter("status", MachineStatus.AVAILABLE)
	            .getResultList();

	    return allAvailableMachines.stream()
	            .filter(machine -> machine.getClass().getSimpleName().equals(machineType))
	            .map(this::convertToDTO)
	            .collect(Collectors.toList());
	}

	private MachineDTO convertToDTO(Machine machine) {
		MachineDTO dto = new MachineDTO(machine.getId(), 
				machine.getClass().getSimpleName(), 
				machine.getStatus().toString());

		dto.setInputProcessId(machine.getInputProcessId());
		dto.setOutputProcessId(machine.getOutputProcessId());
		dto.setActiveProcessId(machine.getActiveProcessId());
		dto.setHasInput(machine.hasInput());
		dto.setHasOutput(machine.hasOutput());
		dto.setProcessingTimeMillis(machine.getProcessingTimeMillis());
		dto.setActionMessage(machine.getActionMessage());

		return dto;
	}

	private static class MachineCompletionInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private final UUID processId;
		private final UUID machineId;

		public MachineCompletionInfo(UUID processId, UUID machineId) {
			this.processId = processId;
			this.machineId = machineId;
		}

		public UUID getProcessId() { return processId; }
		public UUID getMachineId() { return machineId; }
	}

	private static class MachineProcessingInfo implements Serializable {
	    private static final long serialVersionUID = 1L;
	    private final UUID processId;
	    private final UUID machineId;
	    private final UUID itemId;

	    public MachineProcessingInfo(UUID processId, UUID machineId, UUID itemId) {
	        this.processId = processId;
	        this.machineId = machineId;
	        this.itemId = itemId;
	    }

	    public UUID getProcessId() { return processId; }
	    public UUID getMachineId() { return machineId; }
	    public UUID getItemId() { return itemId; }
	}
}