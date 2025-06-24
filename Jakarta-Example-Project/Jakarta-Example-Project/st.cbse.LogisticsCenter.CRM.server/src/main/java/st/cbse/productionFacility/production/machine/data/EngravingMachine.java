package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ENGRAVING")
public class EngravingMachine extends Machine {

    @Override
    protected long shutdownDelayMillis() {
        return 1_000;
    }

    @Override
    protected String buildMessage(String action) {
        return "Engraver " + getName() + " (" + getId() + ") " + action;
    }
}