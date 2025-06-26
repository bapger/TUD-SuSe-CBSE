package st.cbse.productionFacility.production.machine.interfaces;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.dto.MachineDTO;

@Local
public interface IMachineMgmt {
	Machine getMachine(UUID machineId);
	MachineDTO getMachineDTO(UUID machineId);
	
    List<Machine> listAllMachines();
    List<Machine> findAvailableMachinesByType(String machineType);
    List<MachineDTO> findAvailableMachineDTOsByType(String machineType);
    
    boolean reserveMachine(UUID machineId, UUID processId);
    boolean programMachine(UUID machineId);
    boolean executeMachine(UUID machineId, UUID processId);
    boolean stopMachine(UUID machineId);
    boolean canAcceptInput(UUID machineId);
    boolean pauseMachine(UUID machineId);
    boolean resumeMachine(UUID machineId, UUID processId);
    boolean isMachinePausedForProcess(UUID machineId, UUID processId);
    
    UUID retrieveFromOutput(UUID machineId);
    
    void notifyItemArrived(UUID machineId, UUID itemId);
    void clearOutput(UUID machineId);    
}