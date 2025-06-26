package st.cbse.productionFacility.production.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Local;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.step.data.Step;

@Local
public interface IProductionMgmt {
    List<Machine> viewMachines();
    
    UUID reserveMachine(Step type, UUID processId);
    
    boolean programMachine(UUID machineId, UUID processId);
    boolean executeMachine(UUID machineId);
    boolean stopMachine(UUID machineId);
    boolean transportItem(UUID itemId, UUID processId, UUID fromMachineId, UUID toMachineId);
    boolean deliverToStorage(UUID itemId, UUID processId, UUID fromMachineId);
}