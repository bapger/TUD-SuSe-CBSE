package st.cbse.productionFacility.production.beans;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import st.cbse.productionFacility.production.interfaces.IProductionMgmt;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;
import st.cbse.productionFacility.production.machine.data.Machine;
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
    private IProcessMgmt processMgmt;

    @EJB
    private IStorageMgmt storageMgmt;

    @Override
    public List<Machine> viewMachines() {
        return machineMgmt.listAllMachines();
    }

    @Override
    public UUID reserveMachine(Step step, UUID processId) {
        if (step == null) {
            LOG.warning("Cannot reserve machine for null step");
            return null;
        }
        
        String machineType = mapStepToMachineType(step);
        List<Machine> availableMachines = machineMgmt.findAvailableMachinesByType(machineType);
        
        if (availableMachines.isEmpty()) {
            LOG.warning("No available machine for type: " + machineType);
            return null;
        }
        
        Machine machine = availableMachines.get(0);
        boolean reserved = machineMgmt.reserveMachine(machine.getId(), processId);
        
        if (reserved) {
            LOG.info("Reserved machine " + machine.getId() + " for process " + processId);
            return machine.getId();
        }
        
        return null;
    }

    @Override
    public boolean programMachine(UUID machineId, UUID processId) {
        if (machineId == null || processId == null) {
            return false;
        }
        
        boolean programmed = machineMgmt.programMachine(machineId);
        if (programmed) {
            LOG.info("Programmed machine " + machineId + " for process " + processId);
        }
        return programmed;
    }

    @Override
    public boolean executeMachine(UUID machineId) {
        if (machineId == null) {
            return false;
        }
        
        boolean executed = machineMgmt.executeMachine(machineId);
        if (executed) {
            LOG.info("Started execution on machine " + machineId);
        }
        return executed;
    }

    @Override
    public boolean stopMachine(UUID machineId) {
        if (machineId == null) {
            return false;
        }
        
        boolean stopped = machineMgmt.stopMachine(machineId);
        if (stopped) {
            LOG.info("Stopped machine " + machineId);
        }
        return stopped;
    }

    @Override
    public boolean transportItem(UUID itemId, UUID processId, UUID fromMachineId, UUID toMachineId) {
        if (itemId == null || processId == null || fromMachineId == null || toMachineId == null) {
            LOG.warning("Invalid transport parameters");
            return false;
        }
        
        Machine fromMachine = machineMgmt.getMachine(fromMachineId);
        Machine toMachine = machineMgmt.getMachine(toMachineId);
        
        if (fromMachine == null || toMachine == null) {
            LOG.warning("Source or destination machine not found");
            return false;
        }
        
        UUID outputItem = machineMgmt.retrieveFromOutput(fromMachineId);
        if (outputItem == null) {
            LOG.warning("No item ready for transport at machine " + fromMachineId);
            return false;
        }
        
        if (!machineMgmt.canAcceptInput(toMachineId)) {
            LOG.warning("Target machine " + toMachineId + " cannot accept input");
            return false;
        }
        
        storageMgmt.updateItemLocation(itemId, "In transit from " + fromMachine.getMachineType() + " to " + toMachine.getMachineType());
        
        try {
            Thread.sleep(3000); // 3 secondes de transport
        } catch (InterruptedException e) {
            LOG.warning("Transport interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        machineMgmt.notifyItemArrived(toMachineId, itemId);
        storageMgmt.updateItemLocation(itemId, "At " + toMachine.getMachineType());
        processMgmt.notifyStepCompleted(processId, fromMachineId);
        
        LOG.info("Item transported from " + fromMachine.getMachineType() + " to " + toMachine.getMachineType());
        return true;
    }

    @Override
    public boolean deliverToStorage(UUID itemId, UUID processId, UUID fromMachineId) {
        if (itemId == null || processId == null || fromMachineId == null) {
            LOG.warning("Invalid storage delivery parameters");
            return false;
        }
        
        Machine fromMachine = machineMgmt.getMachine(fromMachineId);
        if (fromMachine == null) {
            LOG.warning("Source machine not found");
            return false;
        }
        
        UUID outputItem = machineMgmt.retrieveFromOutput(fromMachineId);
        if (outputItem == null) {
            LOG.warning("No item ready for storage at machine " + fromMachineId);
            return false;
        }
        
        if (!storageMgmt.hasStorageSpace()) {
            LOG.warning("No storage space available");
            return false;
        }
        
        storageMgmt.updateItemLocation(itemId, "In transit to storage");
        
        try {
            Thread.sleep(2000); // 2 secondes vers le stockage
        } catch (InterruptedException e) {
            LOG.warning("Storage delivery interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        storageMgmt.convertToFinished(processId);
        storageMgmt.updateItemLocation(itemId, "In storage");
        processMgmt.notifyStepCompleted(processId, fromMachineId);
        
        LOG.info("Item delivered to storage");
        return true;
    }

    @Timeout
    public void onTransportTimer(Timer timer) {
        UUID transportId = (UUID) timer.getInfo();
        if (transportId == null) {
            LOG.warning("Timer info is null");
            return;
        }
        
        Transport transport = em.find(Transport.class, transportId);
        
        if (transport == null || transport.getStatus() != TransportStatus.IN_TRANSIT) {
            return;
        }
        
        if (transport.getToMachineId() != null) {
            Machine toMachine = machineMgmt.getMachine(transport.getToMachineId());
            machineMgmt.notifyItemArrived(transport.getToMachineId(), transport.getItemId());
            
            storageMgmt.updateItemLocation(transport.getItemId(), "At " + toMachine.getMachineType());
            processMgmt.notifyStepCompleted(transport.getProcessId(), transport.getFromMachineId());
            
            LOG.info("Item " + transport.getItemId() + " delivered to " + toMachine.getMachineType());
        } else {
            storageMgmt.convertToFinished(transport.getProcessId());
            storageMgmt.updateItemLocation(transport.getItemId(), "In storage");
            processMgmt.notifyStepCompleted(transport.getProcessId(), transport.getFromMachineId());
            
            LOG.info("Item " + transport.getItemId() + " delivered to storage");
        }
        
        transport.setStatus(TransportStatus.DELIVERED);
        em.merge(transport);
    }

    @Schedule(hour="*", minute="*", second="*/10", persistent=false)
    public void checkPendingTransports() {
        List<Transport> pendingTransports = em.createQuery(
            "SELECT t FROM Transport t WHERE t.status = :status ORDER BY t.createdAt",
            Transport.class)
            .setParameter("status", TransportStatus.PENDING)
            .setMaxResults(5)
            .getResultList();
            
        LOG.fine("Checking " + pendingTransports.size() + " pending transports");
    }

    private String mapStepToMachineType(Step step) {
        if (step.getType() == null) {
            return "UNKNOWN";
        }
        
        switch (step.getType()) {
            case PRINTING_3D:
                return "PRINTING";
            case PAINTING:
                return "PAINT";
            case SMOOTHING:
                return "SMOOTHING";
            case ENGRAVING:
                return "ENGRAVING";
            case PACKAGING:
                return "PACKAGING";
            default:
                LOG.warning("Unknown step type: " + step.getType());
                return "UNKNOWN";
        }
    }
}