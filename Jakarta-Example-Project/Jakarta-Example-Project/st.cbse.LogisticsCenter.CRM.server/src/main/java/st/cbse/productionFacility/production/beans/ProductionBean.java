package st.cbse.productionFacility.production.beans;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Stateless;
import st.cbse.productionFacility.production.interfaces.IProductionMgmt;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;
import st.cbse.productionFacility.step.data.StepType;

@Stateless
public class ProductionBean implements IProductionMgmt{

	@Override
	public List<Machine> viewMachines() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID reserveMachine(StepType stepType, UUID processId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean programMachine(UUID machineId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean executeMachine(UUID machineId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopMachine(UUID machineId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean transportItem(UUID itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MachineStatus viewStatus(UUID machineId) {
		// TODO Auto-generated method stub
		return null;
	}

}
