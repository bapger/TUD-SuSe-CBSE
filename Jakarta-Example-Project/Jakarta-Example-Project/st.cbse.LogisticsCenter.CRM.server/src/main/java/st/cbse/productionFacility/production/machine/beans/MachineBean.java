package st.cbse.productionFacility.production.machine.beans;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;

@Stateless
public class MachineBean implements IMachineMgmt {

    private static final Logger LOG = Logger.getLogger(MachineBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IProcessMgmt processMgmt;

    @Override
    public boolean executeMachine(UUID machineId) {
        Machine machine = em.find(Machine.class, machineId);
        if (machine == null || machine.getStatus() != MachineStatus.RESERVED) {
            return false;
        }

        machine.startProcessing();
        em.merge(machine);
        LOG.info("Machine " + machineId + " processing - " + machine.getActionMessage());
        
        try {
            Thread.sleep(machine.getProcessingTimeMillis());
        } catch (InterruptedException e) {
            LOG.warning("Machine processing interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        UUID processId = machine.getActiveProcessId();
        machine.finishProcessing();
        em.merge(machine);
        
        if (processId != null && processMgmt != null) {
            processMgmt.notifyStepCompleted(processId, machineId);
        }
        
        LOG.info("Machine " + machineId + " completed processing");
        return true;
    }

    @Override
    public List<Machine> listAllMachines() {
        return em.createQuery("SELECT m FROM Machine m", Machine.class).getResultList();
    }

    @Override
    public Machine getMachine(UUID machineId) {
        return em.find(Machine.class, machineId);
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
    public boolean reserveMachine(UUID machineId, UUID processId) {
        Machine machine = em.find(Machine.class, machineId);
        if (machine == null || machine.getStatus() != MachineStatus.AVAILABLE) {
            return false;
        }
        
        machine.setInputProcessId(processId);
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
        Machine machine = em.find(Machine.class, machineId);
        if (machine == null) return false;
        
        machine.reset();
        em.merge(machine);
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
        return machine != null 
            && machine.hasInput() 
            && machine.getInputProcessId() == null
            && machine.getStatus() == MachineStatus.AVAILABLE;
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
}