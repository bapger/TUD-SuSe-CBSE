package st.cbse.productionFacility.production.interfaces;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.production.dto.MachineData;
import st.cbse.productionFacility.step.data.StepType;

@Local
public interface IProductionMgmt {

    List<MachineData> viewMachines();

    UUID reserveMachineForStep(StepType stepType, UUID processId);
    boolean programMachine(UUID machineId, UUID processId);
    boolean executeMachine(UUID machineId);
    boolean stopMachine(UUID machineId);
    boolean transportItem(UUID itemId);
}