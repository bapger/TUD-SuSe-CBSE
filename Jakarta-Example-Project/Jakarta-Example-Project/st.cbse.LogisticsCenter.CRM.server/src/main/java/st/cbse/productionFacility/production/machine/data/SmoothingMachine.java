package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SMOOTHING")
public class SmoothingMachine extends Machine {

    @Override
    protected long shutdownDelayMillis() {
        return 2_500;
    }

    @Override
    protected String buildMessage(String action) {
        return "Smoother " + getName() + " (" + getId() + ") " + action;
    }
}