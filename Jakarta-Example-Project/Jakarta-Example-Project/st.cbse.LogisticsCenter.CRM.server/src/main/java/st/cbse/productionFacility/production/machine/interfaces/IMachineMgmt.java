package st.cbse.productionFacility.production.machine.interfaces;

import java.util.List;
import java.util.UUID;

import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;

public interface IMachineMgmt {

    List<Machine> viewMachines();
    UUID reserveMachine(Class<? extends Machine> type, UUID processId);
    boolean programMachine(UUID machineId);
    boolean executeMachine(UUID machineId);
    boolean stopMachine(UUID machineId);
    boolean transportItem(UUID itemId);
    MachineStatus viewStatus(UUID machineId);
}