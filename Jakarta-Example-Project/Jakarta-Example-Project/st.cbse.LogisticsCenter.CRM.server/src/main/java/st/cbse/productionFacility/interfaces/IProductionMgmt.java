package st.cbse.productionFacility.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Local;

@Local
public interface IProductionMgmt {
    List<MachineData> viewMachines();
    UUID           reserveMachine(MachineType type, UUID processId);
    boolean        programMachine(UUID machineId, UUID processId);
    boolean        executeMachine(UUID machineId);
    boolean        stopMachine(UUID machineId);
    boolean        transportItem(UUID itemId);
}