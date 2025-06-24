package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PAINT")
public class PaintMachine extends Machine {

    @Override
    protected long shutdownDelayMillis() {
        return 1_500;
    }

    @Override
    protected String buildMessage(String action) {
        return "Painter " + getName() + " (" + getId() + ") " + action;
    }
}