package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ENGRAVING")
public class EngravingMachine extends Machine {
    
    private static final long PROCESSING_TIME = 4000;
    private static final String ACTION_MESSAGE = "Engraving";
    private static final String MACHINE_TYPE = "ENGRAVING";
    
    protected EngravingMachine() {
        super(true, true);
    }
    
    public EngravingMachine(boolean hasInput, boolean hasOutput) {
        super(hasInput, hasOutput);
    }
    
    @Override
    public long getProcessingTimeMillis() {
        return PROCESSING_TIME;
    }
    
    @Override
    public String getActionMessage() {
        return ACTION_MESSAGE;
    }
    
    @Override
    public String getMachineType() {
        return MACHINE_TYPE;
    }
}