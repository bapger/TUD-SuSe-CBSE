package st.cbse.productionFacility.production.interfaces;

import java.util.List;
import java.util.UUID;

import st.cbse.productionFacility.production.machine.data.MachineStatus;
import st.cbse.productionFacility.step.data.StepType;
import st.cbse.productionFacility.production.machine.data.Machine;

public interface IProductionMgmt {
    List<Machine> viewMachines();
    UUID reserveMachine(StepType stepType, UUID processId);
    boolean programMachine(UUID machineId);
    boolean executeMachine(UUID machineId);
    boolean stopMachine(UUID machineId);
    boolean transportItem(UUID itemId);
    MachineStatus viewStatus(UUID machineId);
}