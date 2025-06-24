package st.cbse.productionFacility.step.beans;

import java.util.Map;
import java.util.UUID;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import st.cbse.productionFacility.production.interfaces.IProductionMgmt;
import st.cbse.productionFacility.step.data.Step;
import st.cbse.productionFacility.step.data.StepStatus;
import st.cbse.productionFacility.step.data.StepType;
import st.cbse.productionFacility.step.interfaces.IStepMgmt;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class StepBean implements IStepMgmt {

    @PersistenceContext(unitName = "prodPU")
    private EntityManager em;

    @EJB
    private IProductionMgmt production;

    @Override
    public boolean addStep(StepType type, UUID processId, Map<String, String> parameters) {
        Step step = new Step(type, processId, parameters);
        em.persist(step);
        return true;
    }

    @Override
    public boolean executeStep(UUID stepId) {

        Step step = em.find(Step.class, stepId);
        if (step == null || step.getStatus() != StepStatus.PENDING) {
            return false;
        }

        UUID machineId = production.reserveMachineForStep(step.getType(), step.getProcessId());
        if (machineId == null) {
            return false;                       // aucune machine dispo
        }

        step.setMachineId(machineId);
        step.setStatus(StepStatus.IN_PROGRESS);

        production.programMachine(machineId, step.getProcessId());
        production.executeMachine(machineId);

        step.setStatus(StepStatus.FINISHED);
        return true;
    }
}