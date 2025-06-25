package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SMOOTHINGS")
public class SmoothingMachine extends Machine {
    
    private static final long PROCESSING_TIME = 2000;
    private static final String ACTION_MESSAGE = "Smoothing";
    private static final String MACHINE_TYPE = "SMOOTHING";
    
    protected SmoothingMachine() {
        super(true, true);
    }
    
    public SmoothingMachine(boolean hasInput, boolean hasOutput) {
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