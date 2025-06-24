package st.cbse.productionFacility.production.machine.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.productionFacility.production.machine.data.*;

@Stateless
class MachineBean {
    public void simulateProcessing() {
        try {
            Thread.sleep(3000); // simulate 3 seconds of work
        } catch (InterruptedException ignored) {}
    }
}
