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
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;

@Stateless
public class MachineBean implements IMachineMgmt {

	private static final Logger LOG = Logger.getLogger(MachineBean.class.getName());

	@PersistenceContext
	private EntityManager em;

	@EJB
	private IProcessMgmt processMgmt;

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
	public boolean executeMachine(UUID machineId, UUID processId) {
	    Machine machine = em.find(Machine.class, machineId);
	    if (machine == null || machine.getStatus() != MachineStatus.RESERVED) {
	        LOG.warning("Cannot execute machine " + machineId + " - machine null or not reserved");
	        return false;
	    }

	    LOG.info("Machine " + machineId + " before processing - outputProcessId: " + machine.getOutputProcessId());
	    LOG.info("Machine " + machineId + " starting processing - hasOutput: " + machine.hasOutput());
	    
	    // Pour les machines sans input, définir directement l'activeProcessId
	    if (!machine.hasInput()) {
	        machine.setActiveProcessId(processId);
	        machine.setStatus(MachineStatus.ACTIVE);
	    } else {
	        // Pour les machines avec input, utiliser la méthode normale
	        boolean started = machine.startProcessing();
	        if (!started) {
	            LOG.warning("Failed to start processing on machine " + machineId);
	            return false;
	        }
	    }
	    
	    em.merge(machine);
	    LOG.info("Machine " + machineId + " processing - " + machine.getActionMessage());
	    
	    try {
	        Thread.sleep(machine.getProcessingTimeMillis());
	    } catch (InterruptedException e) {
	        LOG.warning("Machine processing interrupted: " + e.getMessage());
	        Thread.currentThread().interrupt();
	        return false;
	    }
	    
	    UUID activeProcessId = machine.getActiveProcessId();
	    LOG.info("Machine " + machineId + " finishing - activeProcessId: " + activeProcessId);
	    
	    boolean finished = machine.finishProcessing();
	    LOG.info("Machine " + machineId + " finishProcessing returned: " + finished);
	    LOG.info("Machine " + machineId + " after finish - outputProcessId: " + machine.getOutputProcessId());
	    LOG.info("Machine " + machineId + " after finish - status: " + machine.getStatus());
	    
	    em.merge(machine);
	    em.flush();
	    
	    Machine verifyMachine = em.find(Machine.class, machineId);
	    LOG.info("Machine " + machineId + " verification after flush - outputProcessId: " + verifyMachine.getOutputProcessId());
	    
	    LOG.info("Machine " + machineId + " completed processing");
	    
	    if (activeProcessId != null && processMgmt != null) {
	        timerService.createSingleActionTimer(500, new TimerConfig(
	            new MachineCompletionInfo(activeProcessId, machineId), false));
	    }
	    
	    return true;
	}

	// Ajouter cette méthode timeout dans MachineBean
	@Timeout
	public void handleMachineCompletion(Timer timer) {
		MachineCompletionInfo info = (MachineCompletionInfo) timer.getInfo();
		if (info != null) {
			LOG.info("Notifying process " + info.getProcessId() + 
					" that machine " + info.getMachineId() + " completed");
			processMgmt.notifyStepCompleted(info.getProcessId(), info.getMachineId());
		}
	}

	// Classe interne pour stocker les infos
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

	@Override
	public List<Machine> listAllMachines() {
		return em.createQuery("SELECT m FROM Machine m", Machine.class).getResultList();
	}

	@Override
	public List<Machine> findAvailableMachinesByType(String machineType) {
		return em.createQuery(
				"SELECT m FROM Machine m WHERE m.status = :status AND TYPE(m) = :type", 
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
		}
		return processId;
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
		// Récupérer toutes les machines disponibles
		List<Machine> allAvailableMachines = em.createQuery(
				"SELECT m FROM Machine m WHERE m.status = :status", 
				Machine.class)
				.setParameter("status", MachineStatus.AVAILABLE)
				.getResultList();

		// Filtrer par type de classe en Java
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

}