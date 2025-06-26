package st.cbse.productionFacility.production.machine.dto;

import java.io.Serializable;
import java.util.UUID;


public class MachineDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String type;
    private String status;
    
    private UUID inputProcessId;
    private UUID outputProcessId;
    private UUID activeProcessId;
    
    private boolean hasInput;
    private boolean hasOutput;
    
    private long processingTimeMillis;
    private String actionMessage;
    
    public MachineDTO() {
    }
    
    public MachineDTO(UUID id, String type, String status) {
        this.id = id;
        this.type = type;
        this.status = status;
    }
   
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public UUID getInputProcessId() {
        return inputProcessId;
    }
    
    public void setInputProcessId(UUID inputProcessId) {
        this.inputProcessId = inputProcessId;
    }
    
    public UUID getOutputProcessId() {
        return outputProcessId;
    }
    
    public void setOutputProcessId(UUID outputProcessId) {
        this.outputProcessId = outputProcessId;
    }
    
    public UUID getActiveProcessId() {
        return activeProcessId;
    }
    
    public void setActiveProcessId(UUID activeProcessId) {
        this.activeProcessId = activeProcessId;
    }
    
    public boolean isHasInput() {
        return hasInput;
    }
    
    public void setHasInput(boolean hasInput) {
        this.hasInput = hasInput;
    }
    
    public boolean isHasOutput() {
        return hasOutput;
    }
    
    public void setHasOutput(boolean hasOutput) {
        this.hasOutput = hasOutput;
    }
    
    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }
    
    public void setProcessingTimeMillis(long processingTimeMillis) {
        this.processingTimeMillis = processingTimeMillis;
    }
    
    public String getActionMessage() {
        return actionMessage;
    }
    
    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }
    
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }
    
    public boolean isReserved() {
        return "RESERVED".equals(status);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public boolean hasProcessWaiting() {
        return inputProcessId != null;
    }
    
    public boolean hasProcessCompleted() {
        return outputProcessId != null;
    }
    
    @Override
    public String toString() {
        return String.format("MachineDTO[id=%s, type=%s, status=%s, active=%s]", 
            id, type, status, activeProcessId);
    }
}