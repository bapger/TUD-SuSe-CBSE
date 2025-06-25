package st.cbse.productionFacility.production.machine.interfaces;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.production.machine.data.Machine;

@Local
public interface IMachineMgmt {
    
    List<Machine> listAllMachines();
    Machine getMachine(UUID machineId);
    List<Machine> findAvailableMachinesByType(String machineType);
    
    boolean reserveMachine(UUID machineId, UUID processId);
    boolean programMachine(UUID machineId);
    boolean executeMachine(UUID machineId);
    boolean stopMachine(UUID machineId);
    
    UUID retrieveFromOutput(UUID machineId);
    boolean canAcceptInput(UUID machineId);
    
    void notifyItemArrived(UUID machineId, UUID itemId);
}