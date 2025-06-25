package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PRINTER")
public class PrintingMachine extends Machine {
    
    private static final long PROCESSING_TIME = 2000;
    private static final String ACTION_MESSAGE = "Printing";
    private static final String MACHINE_TYPE = "PRINTER";
    
    protected PrintingMachine() {
        super(false, true);
    }
    
    public PrintingMachine(boolean hasInput, boolean hasOutput) {
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