package st.cbse.productionFacility.production.machine.interfaces;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.dto.MachineDTO;

@Local
public interface IMachineMgmt {
    
    List<Machine> listAllMachines();
    Machine getMachine(UUID machineId);
    
    // Méthode existante
    List<Machine> findAvailableMachinesByType(String machineType);
    
    // Nouvelle méthode pour les DTOs
    List<MachineDTO> findAvailableMachineDTOsByType(String machineType);
    
    boolean reserveMachine(UUID machineId, UUID processId);
    boolean programMachine(UUID machineId);
    boolean executeMachine(UUID machineId, UUID processId);
    boolean stopMachine(UUID machineId);
    
    UUID retrieveFromOutput(UUID machineId);
    boolean canAcceptInput(UUID machineId);
    
    void notifyItemArrived(UUID machineId, UUID itemId);
    void clearOutput(UUID machineId);
    MachineDTO getMachineDTO(UUID machineId);
    
    boolean pauseMachine(UUID machineId);
    boolean resumeMachine(UUID machineId, UUID processId);
    boolean isMachinePausedForProcess(UUID machineId, UUID processId);
}