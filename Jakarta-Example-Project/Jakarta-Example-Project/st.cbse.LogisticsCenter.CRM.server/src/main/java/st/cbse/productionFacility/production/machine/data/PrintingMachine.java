package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PRINTING")
public class PrintingMachine extends Machine {

    @Override
    protected long shutdownDelayMillis() {
        return 3_000;
    }

    @Override
    protected String buildMessage(String action) {
        return "Printer " + getName() + " (" + getId() + ") " + action;
    }
}