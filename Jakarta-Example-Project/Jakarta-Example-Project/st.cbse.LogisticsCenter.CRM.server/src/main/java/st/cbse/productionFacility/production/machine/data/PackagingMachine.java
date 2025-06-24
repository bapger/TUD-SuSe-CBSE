package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PACKAGING")
public class PackagingMachine extends Machine {

    @Override
    protected long shutdownDelayMillis() {
        return 2_000;
    }

    @Override
    protected String buildMessage(String action) {
        return "Packager" + getName() + " (" + getId() + ") " + action;
    }
}