package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
public abstract class Machine {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID inputProcessId;
    
    private UUID outputProcessId;
    
    private UUID activeProcessId;

    @Enumerated(EnumType.STRING)
    private MachineStatus status = MachineStatus.AVAILABLE;

    @Transient
    private boolean hasInput = true;
    
    @Transient
    private boolean hasOutput = true;

    protected Machine() { }

    protected Machine(boolean hasInput, boolean hasOutput) {
        this.hasInput = hasInput;
        this.hasOutput = hasOutput;
    }

    /* ----------- Getters/Setters ----------- */
    
    public UUID getId() { return id; }
    
    public UUID getInputProcessId() { return inputProcessId; }
    public void setInputProcessId(UUID processId) { this.inputProcessId = processId; }
    
    public UUID getOutputProcessId() { return outputProcessId; }
    public void setOutputProcessId(UUID processId) { this.outputProcessId = processId; }
    
    public UUID getActiveProcessId() { return activeProcessId; }
    public void setActiveProcessId(UUID processId) { this.activeProcessId = processId; }
    
    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }
    
    public boolean hasInput() { return hasInput; }
    public boolean hasOutput() { return hasOutput; }


    @Transient
    public abstract long getProcessingTimeMillis();
    
    @Transient
    public abstract String getActionMessage();
    
    @Transient
    public abstract String getMachineType();

    public void reset() {
        this.inputProcessId = null;
        this.outputProcessId = null;
        this.activeProcessId = null;
        this.status = MachineStatus.AVAILABLE;
    }
    
    public boolean startProcessing() {
        if (inputProcessId == null || status != MachineStatus.RESERVED) {
            return false;
        }
        activeProcessId = inputProcessId;
        inputProcessId = null;
        status = MachineStatus.ACTIVE;
        return true;
    }
    
    public boolean finishProcessing() {
        if (activeProcessId == null || status != MachineStatus.ACTIVE) {
            return false;
        }
        if (hasOutput) {
            outputProcessId = activeProcessId;
        }
        activeProcessId = null;
        status = MachineStatus.AVAILABLE;
        return true;
    }
}