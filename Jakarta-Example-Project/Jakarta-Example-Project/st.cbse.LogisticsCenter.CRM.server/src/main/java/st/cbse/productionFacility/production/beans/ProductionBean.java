package st.cbse.productionFacility.production.beans;

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

import st.cbse.productionFacility.production.interfaces.IProductionMgmt;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.productionFacility.process.dto.ProcessDTO;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;
import st.cbse.productionFacility.step.data.Step;

import st.cbse.productionFacility.production.data.Transport;
import st.cbse.productionFacility.production.data.Transport.TransportStatus;

@Stateless
public class ProductionBean implements IProductionMgmt {

	private static final Logger LOG = Logger.getLogger(ProductionBean.class.getName());

	@PersistenceContext
	private EntityManager em;

	@Resource
	private TimerService timerService;

	@EJB
	private IMachineMgmt machineMgmt;
	
	@EJB
	private IOrderMgmt orderMgmt;

	@EJB
	private IProcessMgmt processMgmt;

	@EJB
	private IStorageMgmt storageMgmt;

	@Override
	public List<Machine> viewMachines() {
		LOG.info("Retrieving all machines from the system");
		List<Machine> machines = machineMgmt.listAllMachines();
		LOG.info("Found " + machines.size() + " machines in total");
		return machines;
	}

	@Override
	public UUID reserveMachine(Step step, UUID processId) {
		if (step == null) {
			LOG.warning("Cannot reserve machine for null step");
			return null;
		}

		LOG.info("Attempting to reserve machine for step type: " + step.getType() + " and process: " + processId);

		String machineType = mapStepToMachineType(step);
		List<Machine> availableMachines = machineMgmt.findAvailableMachinesByType(machineType);

		LOG.info("Found " + availableMachines.size() + " available machines of type " + machineType);

		if (availableMachines.isEmpty()) {
			LOG.warning("No available machine for type: " + machineType);
			return null;
		}

		Machine machine = availableMachines.get(0);
		LOG.info("Attempting to reserve machine " + machine.getId() + " of type " + machine.getMachineType());

		boolean reserved = machineMgmt.reserveMachine(machine.getId(), processId);

		if (reserved) {
			LOG.info("Reserved machine " + machine.getId() + " for process " + processId);
			return machine.getId();
		} else {
			LOG.warning("Failed to reserve machine " + machine.getId() + " for process " + processId);
		}

		return null;
	}

	@Override
	public boolean programMachine(UUID machineId, UUID processId) {
		if (machineId == null || processId == null) {
			LOG.warning("Cannot program machine with null parameters - machineId: " + machineId + ", processId: " + processId);
			return false;
		}

		LOG.info("Attempting to program machine " + machineId + " for process " + processId);

		boolean programmed = machineMgmt.programMachine(machineId);
		if (programmed) {
			LOG.info("Programmed machine " + machineId + " for process " + processId);
		} else {
			LOG.warning("Failed to program machine " + machineId);
		}
		return programmed;
	}

	@Override
	public boolean executeMachine(UUID machineId) {
	    if (machineId == null) {
	        LOG.warning("Cannot execute machine with null ID");
	        return false;
	    }

	    LOG.info("Attempting to execute machine " + machineId);

	    // PROBLÈME : On n'a pas le processId ici !
	    // Cette méthode fait partie de l'interface IProductionMgmt
	    // Il faut soit :
	    // 1. Modifier l'interface pour accepter processId
	    // 2. Ou récupérer le processId depuis la machine
	    
	    // Solution temporaire : récupérer depuis la machine
	    Machine machine = machineMgmt.getMachine(machineId);
	    if (machine == null) {
	        LOG.warning("Machine not found: " + machineId);
	        return false;
	    }
	    
	    UUID processId = machine.getInputProcessId();
	    
	    boolean executed = machineMgmt.executeMachine(machineId, processId);
	    if (executed) {
	        LOG.info("Started execution on machine " + machineId);
	    } else {
	        LOG.warning("Failed to execute machine " + machineId);
	    }
	    return executed;
	}

	@Override
	public boolean stopMachine(UUID machineId) {
		if (machineId == null) {
			LOG.warning("Cannot stop machine with null ID");
			return false;
		}

		LOG.info("Attempting to stop machine " + machineId);

		boolean stopped = machineMgmt.stopMachine(machineId);
		if (stopped) {
			LOG.info("Stopped machine " + machineId);
		} else {
			LOG.warning("Failed to stop machine " + machineId);
		}
		return stopped;
	}

	@Override
	public boolean transportItem(UUID itemId, UUID processId, UUID fromMachineId, UUID toMachineId) {
		if (itemId == null || processId == null || fromMachineId == null || toMachineId == null) {
			LOG.warning("Invalid transport parameters - itemId: " + itemId + ", processId: " + processId + 
					", fromMachineId: " + fromMachineId + ", toMachineId: " + toMachineId);
			return false;
		}

		LOG.info("Starting transport process for item " + itemId + " of process " + processId);

		Machine fromMachine = machineMgmt.getMachine(fromMachineId);
		Machine toMachine = machineMgmt.getMachine(toMachineId);

		if (fromMachine == null || toMachine == null) {
			LOG.warning("Source or destination machine not found - fromMachine: " + 
					(fromMachine != null ? "found" : "null") + ", toMachine: " + 
					(toMachine != null ? "found" : "null"));
			return false;
		}

		LOG.info("Initiating transport: Item " + itemId + " from " + fromMachine.getMachineType() + 
				" (ID: " + fromMachineId + ") to " + toMachine.getMachineType() + " (ID: " + toMachineId + ")");

		UUID outputItem = machineMgmt.retrieveFromOutput(fromMachineId);
		if (outputItem == null) {
			LOG.warning("No item ready for transport at machine " + fromMachineId);
			return false;
		}

		LOG.info("Retrieved item " + outputItem + " from machine " + fromMachineId);

		// Vérifier le statut de la machine cible
		MachineStatus toMachineStatus = toMachine.getStatus();

		if (toMachineStatus == MachineStatus.RESERVED) {
			LOG.info("Target machine " + toMachineId + " is reserved");
		} else if (toMachineStatus == MachineStatus.AVAILABLE) {
			LOG.info("Target machine " + toMachineId + " is available");
		} else {
			LOG.warning("Target machine " + toMachineId + " has invalid status: " + toMachineStatus);
			return false;
		}

		// Vérifier que la machine peut accepter l'input
		if (toMachine.getInputProcessId() != null) {
			LOG.warning("Target machine " + toMachineId + " already has input: " + toMachine.getInputProcessId());
			return false;
		}

		// Vérifier que la machine a la capacité de recevoir des inputs
		if (!toMachine.hasInput()) {
			LOG.warning("Target machine " + toMachineId + " cannot accept inputs (hasInput=false)");
			return false;
		}

		LOG.info("Target machine " + toMachineId + " can accept input");

		storageMgmt.updateItemLocation(itemId, "In transit from " + fromMachine.getMachineType() + " to " + toMachine.getMachineType());

		// Créer une entité Transport
		Transport transport = new Transport(itemId, processId, fromMachineId, toMachineId);
		transport.setStatus(TransportStatus.IN_TRANSIT);
		em.persist(transport);

		LOG.info("Transport entity created with ID: " + transport.getId() + " - Timer set for 3 seconds");

		timerService.createSingleActionTimer(5000, new TimerConfig(transport.getId(), false));

		LOG.info("Transport initiated from machine " + fromMachineId + " to " + toMachineId);
		return true;
	}

	@Override
	public boolean deliverToStorage(UUID itemId, UUID processId, UUID fromMachineId) {
	    if (itemId == null || processId == null || fromMachineId == null) {
	        LOG.warning("Invalid storage delivery parameters - itemId: " + itemId + 
	                ", processId: " + processId + ", fromMachineId: " + fromMachineId);
	        return false;
	    }

	    LOG.info("Attempting to deliver item " + itemId + " from machine " + fromMachineId + " to storage");

	    Machine fromMachine = machineMgmt.getMachine(fromMachineId);
	    if (fromMachine == null) {
	        LOG.warning("Source machine not found");
	        return false;
	    }

	    LOG.info("Source machine type: " + fromMachine.getMachineType());

	    UUID outputItem = machineMgmt.retrieveFromOutput(fromMachineId);
	    if (outputItem == null) {
	        LOG.warning("No item ready for storage at machine " + fromMachineId);
	        return false;
	    }

	    LOG.info("Retrieved output item: " + outputItem);

	    if (!storageMgmt.hasStorageSpace()) {
	        LOG.warning("No storage space available");
	        return false;
	    }

	    LOG.info("Storage space available - proceeding with delivery");
	    
	    // Créer un transport vers le storage (toMachineId = null)
	    storageMgmt.updateItemLocation(itemId, "In transit to storage");
	    
	    Transport transport = new Transport(itemId, processId, fromMachineId, null);
	    transport.setStatus(TransportStatus.IN_TRANSIT);
	    em.persist(transport);
	    
	    LOG.info("Transport to storage created with ID: " + transport.getId() + " - Timer set for 2 seconds");
	    
	    // Timer de 2 secondes pour le transport vers le storage
	    timerService.createSingleActionTimer(2000, new TimerConfig(transport.getId(), false));
	    
	    return true;
	}

	@Timeout
	public void onTransportTimer(Timer timer) {
	    UUID transportId = (UUID) timer.getInfo();
	    LOG.info("Transport timer triggered for transport ID: " + transportId);

	    if (transportId == null) {
	        LOG.warning("Timer info is null");
	        return;
	    }

	    Transport transport = em.find(Transport.class, transportId);

	    if (transport == null) {
	        LOG.warning("Transport not found for ID: " + transportId);
	        return;
	    }

	    if (transport.getStatus() != TransportStatus.IN_TRANSIT) {
	        LOG.info("Transport " + transportId + " already processed - Status: " + transport.getStatus());
	        return;
	    }

	    // Vérifier si le process est en pause
	    ProcessDTO processDTO = processMgmt.getProcess(transport.getProcessId());
	    if (processDTO != null && "PAUSED".equals(processDTO.getStatus())) {
	        LOG.info("Process " + transport.getProcessId() + " is paused - transport continues to destination");
	    }

	    LOG.info("Processing transport delivery for item " + transport.getItemId());

	    if (transport.getToMachineId() != null) {
	        // Transport vers une autre machine
	        Machine toMachine = machineMgmt.getMachine(transport.getToMachineId());
	        machineMgmt.notifyItemArrived(transport.getToMachineId(), transport.getItemId());
	        storageMgmt.updateItemLocation(transport.getItemId(), "At " + toMachine.getMachineType());

	        processMgmt.notifyStepCompleted(transport.getProcessId(), transport.getFromMachineId());

	        // Vérifier si le process est en pause avant de programmer la machine
	        if (processDTO != null && !"PAUSED".equals(processDTO.getStatus())) {
	            if (machineMgmt.programMachine(transport.getToMachineId())) {
	                machineMgmt.executeMachine(transport.getToMachineId(), transport.getProcessId());
	            }
	        } else {
	            LOG.info("Process is paused - machine " + transport.getToMachineId() + " will wait with item in input");
	            // L'item reste dans l'input de la machine jusqu'à la reprise du process
	        }

	        LOG.info("Item " + transport.getItemId() + " delivered to " + toMachine.getMachineType());
	    } else {
	        // Transport vers le storage
	        LOG.info("Delivering to storage - Converting process " + transport.getProcessId() + " to finished product");
	        
	        // Le transport vers le storage continue même si le process est en pause
	        boolean converted = storageMgmt.convertToFinished(transport.getProcessId());
	        
	        if (converted) {
	            storageMgmt.updateItemLocation(transport.getItemId(), "In storage");
	            processMgmt.notifyStepCompleted(transport.getProcessId(), transport.getFromMachineId());
	            LOG.info("Item " + transport.getItemId() + " successfully stored as finished product");
	            orderMgmt.updateStatusPrintingRequest(processDTO.getPrintRequestId(), "IN_STORAGE");
	        } else {
	            LOG.warning("Failed to convert process " + transport.getProcessId() + " to finished product");
	        }
	    }

	    transport.setStatus(TransportStatus.DELIVERED);
	    em.merge(transport);

	    LOG.info("Transport " + transportId + " marked as DELIVERED");
	}

	//    @Schedule(hour="*", minute="*", second="*/30", persistent=false)
	//    public void checkPendingTransports() {
	//        List<Transport> pendingTransports = em.createQuery(
	//            "SELECT t FROM Transport t WHERE t.status = :status ORDER BY t.createdAt",
	//            Transport.class)
	//            .setParameter("status", TransportStatus.PENDING)
	//            .setMaxResults(5)
	//            .getResultList();
	//            
	//        LOG.fine("Checking " + pendingTransports.size() + " pending transports");
	//    }

	private String mapStepToMachineType(Step step) {
		LOG.fine("Mapping step type to machine type: " + step.getType());

		if (step.getType() == null) {
			LOG.warning("Step type is null - returning UNKNOWN");
			return "UNKNOWN";
		}

		String result;
		switch (step.getType()) {
		case PRINTING_3D:
			LOG.fine("Step type PRINTING_3D mapped to PRINTING machine");
			result = "PRINTING";
			break;
		case PAINTING:
			LOG.fine("Step type PAINTING mapped to PAINT machine");
			result = "PAINT";
			break;
		case SMOOTHING:
			LOG.fine("Step type SMOOTHING mapped to SMOOTHING machine");
			result = "SMOOTHING";
			break;
		case ENGRAVING:
			LOG.fine("Step type ENGRAVING mapped to ENGRAVING machine");
			result = "ENGRAVING";
			break;
		case PACKAGING:
			LOG.fine("Step type PACKAGING mapped to PACKAGING machine");
			result = "PACKAGING";
			break;
		default:
			LOG.warning("Unknown step type: " + step.getType());
			result = "UNKNOWN";
		}

		LOG.fine("Mapped step " + step.getType() + " to machine type: " + result);
		return result;
	}
}