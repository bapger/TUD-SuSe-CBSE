package st.cbse.productionFacility.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.step.data.Step;

@Remote
public interface IProductionMgmt {
List<Machine> viewMachines();
UUID reserveMachine(Step type, UUID processId);
boolean programMachine(UUID machineId, UUID processId);
boolean executeMachine(UUID machineId);
boolean stopMachine(UUID machineId);
boolean transportItem(UUID itemId);
}

