package st.cbse.production.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.production.data.*;

@Stateless
class MachineBean {
    public void simulateProcessing() {
        try {
            Thread.sleep(3000); // simulate 3 seconds of work
        } catch (InterruptedException ignored) {}
    }
}
