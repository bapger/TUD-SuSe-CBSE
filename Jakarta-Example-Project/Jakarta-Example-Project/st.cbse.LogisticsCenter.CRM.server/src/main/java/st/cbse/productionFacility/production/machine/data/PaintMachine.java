package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PAINT")
public class PaintMachine extends Machine {
    
    private static final long PROCESSING_TIME = 2000;
    private static final String ACTION_MESSAGE = "Painting";
    private static final String MACHINE_TYPE = "PAINT";
    
    protected PaintMachine() {
        super(true, true);
    }
    
    public PaintMachine(boolean hasInput, boolean hasOutput) {
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