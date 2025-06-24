package st.cbse.productionFacility.beans;

import java.util.Map;
import java.util.UUID;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.PersistenceContext;
import st.cbse.productionFacility.interfaces.IProductionMgmt;
import st.cbse.productionFacility.interfaces.MachineType;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ProductionBean implements IProductionMgmt {

    @PersistenceContext EntityManager em;
    @EJB MachineComponentLocal machineCmp;

    @Override
    public List<MachineData> viewMachines() {
        return machineCmp.getAllMachinesStatus();          // DTO côté Machine
    }

    @Override
    public UUID reserveMachine(MachineType type, UUID processId) {
        UUID machineId = machineCmp.reserveMachine(type);
        if (machineId != null) {
            Process p = em.find(Process.class, processId);
            p.addReservedMachine(machineId);
        }
        return machineId;
    }

    @Override
    public boolean programMachine(UUID machineId, UUID processId) {
        Process p = em.find(Process.class, processId);
        return p != null && p.programMachine(machineId);
    }

    @Override
    public boolean executeMachine(UUID machineId) {
        return machineCmp.executeMachineOperation(machineId, Map.of())
               .isDone();                                 // Fire-and-forget
    }

    @Override
    public boolean stopMachine(UUID machineId) {
        machineCmp.stopMachine(machineId);
        return true;
    }

    @Override
    public boolean transportItem(UUID itemId) {
        // délègue au ConveyorBeltNetwork bean (non montré ici)
        return true;
    }
    
    @Override
    public UUID reserveMachine(MachineType type, UUID processId) {
    	
    }

}