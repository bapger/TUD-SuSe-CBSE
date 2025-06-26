package st.cbse.productionFacility.step.beans;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import st.cbse.productionFacility.production.interfaces.IProductionMgmt;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.step.data.Step;
import st.cbse.productionFacility.step.data.StepStatus;
import st.cbse.productionFacility.step.data.StepType;
import st.cbse.productionFacility.step.interfaces.IStepMgmt;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class StepBean implements IStepMgmt {

    private static final Logger LOG = Logger.getLogger(StepBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IProductionMgmt production;
    
    @EJB
    private IMachineMgmt machineMgmt;

    @Override
    public boolean addStep(StepType type, UUID processId, Map<String, String> parameters) {
        Step step = new Step(type, processId, parameters);
        em.persist(step);
        LOG.info("Added step " + type + " for process " + processId);
        return true;
    }

    @Override
    public boolean executeStep(UUID stepId) {
        Step step = em.find(Step.class, stepId);
        if (step == null || step.getStatus() != StepStatus.PENDING) {
            LOG.warning("Step " + stepId + " not found or not pending");
            return false;
        }

        String machineType = mapStepTypeToMachineType(step.getType());
        if (machineType == null) {
            LOG.warning("No machine type mapping for step type " + step.getType());
            return false;
        }

        List<Machine> availableMachines = machineMgmt.findAvailableMachinesByType(machineType);
        if (availableMachines.isEmpty()) {
            LOG.warning("No available machines for type " + machineType);
            return false;
        }

        Machine machine = availableMachines.get(0);
        UUID machineId = machine.getId();

        boolean reserved = machineMgmt.reserveMachine(machineId, step.getProcessId());
        if (!reserved) {
            LOG.warning("Failed to reserve machine " + machineId);
            return false;
        }

        step.setMachineId(machineId);
        step.setStatus(StepStatus.IN_PROGRESS);
        em.merge(step);

        boolean programmed = machineMgmt.programMachine(machineId);
        if (!programmed) {
            LOG.warning("Failed to program machine " + machineId);
            step.setStatus(StepStatus.PENDING);
            em.merge(step);
            return false;
        }

        boolean executed = machineMgmt.executeMachine(machineId,step.getProcessId());
        if (!executed) {
            LOG.warning("Failed to execute machine " + machineId);
            step.setStatus(StepStatus.PENDING);
            em.merge(step);
            return false;
        }

        LOG.info("Step " + stepId + " is now in progress on machine " + machineId);
        return true;
    }

    @Override
    public boolean completeStep(UUID stepId) {
        Step step = em.find(Step.class, stepId);
        if (step == null || step.getStatus() != StepStatus.IN_PROGRESS) {
            return false;
        }

        step.setStatus(StepStatus.FINISHED);
        em.merge(step);

        LOG.info("Step " + stepId + " completed");
        return true;
    }

    @Override
    public Step getStep(UUID stepId) {
        return em.find(Step.class, stepId);
    }

    @Override
    public List<Step> getStepsByProcess(UUID processId) {
        return em.createQuery(
            "SELECT s FROM Step s WHERE s.processId = :processId ORDER BY s.createdAt",
            Step.class)
            .setParameter("processId", processId)
            .getResultList();
    }

    @Override
    public List<Step> getPendingSteps() {
        return em.createQuery(
            "SELECT s FROM Step s WHERE s.status = :status ORDER BY s.createdAt",
            Step.class)
            .setParameter("status", StepStatus.PENDING)
            .getResultList();
    }

    private String mapStepTypeToMachineType(StepType stepType) {
        switch (stepType) {
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
                return null;
        }
    }
}