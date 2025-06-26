package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PACKAGING")
public class PackagingMachine extends Machine {
    
    private static final long PROCESSING_TIME = 30000;
    private static final String ACTION_MESSAGE = "Packaging";
    private static final String MACHINE_TYPE = "PACKAGING";
    
    protected PackagingMachine() {
        super(true, true);
    }
    
    public PackagingMachine(boolean hasInput, boolean hasOutput) {
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