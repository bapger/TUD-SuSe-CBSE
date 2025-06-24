package st.cbse.productionFacility.production.machine.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import st.cbse.productionFacility.production.machine.data.EngravingMachine;
import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;
import st.cbse.productionFacility.production.machine.data.PaintMachine;
import st.cbse.productionFacility.production.machine.data.PackagingMachine;
import st.cbse.productionFacility.production.machine.data.PrintingMachine;
import st.cbse.productionFacility.production.machine.data.SmoothingMachine;
import st.cbse.productionFacility.production.machine.interfaces.IMachineMgmt;

@Stateless
public class MachineBean implements IMachineMgmt {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Machine> viewMachines() {
        return em.createQuery("SELECT m FROM Machine m", Machine.class)
                 .getResultStream()
                 .collect(Collectors.toList());
    }

    @Override
    public UUID reserveMachine(Class<? extends Machine> type, UUID processId) {
        Machine machine = em.createQuery(
                        "SELECT m FROM Machine m WHERE TYPE(m)=:cls AND m.status=:status",
                        Machine.class)
                .setParameter("cls", type)
                .setParameter("status", MachineStatus.AVAILABLE)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (machine == null) {
            return null;
        }
        machine.reserve();
        machine.setCurrentProductId(processId);
        em.merge(machine);
        return machine.getId();
    }

    @Override
    public boolean programMachine(UUID machineId) {
        Machine machine = em.find(Machine.class, machineId);
        if (machine == null || machine.getStatus() != MachineStatus.RESERVED) {
            return false;
        }
        System.out.println(machine.message("programmée"));
        return true;
    }

    @Override
    public boolean executeMachine(UUID machineId) {
        Machine machine = em.find(Machine.class, machineId);
        if (machine == null || machine.getStatus() != MachineStatus.RESERVED) {
            return false;
        }
        machine.activate();
        em.merge(machine);
        simulateProcessing();
        return true;
    }

    @Override
    public boolean stopMachine(UUID machineId) {
        Machine machine = em.find(Machine.class, machineId);
        if (machine == null || machine.getStatus() != MachineStatus.ACTIVE) {
            return false;
        }
        delay(machine.getShutdownDelayMillis());
        machine.release();
        em.merge(machine);
        return true;
    }

    @Override
    public boolean transportItem(UUID itemId) {
        System.out.println("Transport de l’item " + itemId);
        return true;
    }

    @Override
    public MachineStatus viewStatus(UUID machineId) {
        Machine machine = em.find(Machine.class, machineId);
        return machine == null ? null : machine.getStatus();
    }

    private void simulateProcessing() {
        delay(3_000);
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}