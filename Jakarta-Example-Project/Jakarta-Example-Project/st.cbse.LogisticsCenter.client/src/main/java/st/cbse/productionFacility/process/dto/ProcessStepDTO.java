package st.cbse.productionFacility.process.dto;

import java.io.Serializable;
import java.util.UUID;

public class ProcessStepDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String stepType;
    private int stepOrder;
    private boolean completed;
    private UUID assignedMachineId;
    
    public ProcessStepDTO() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }
    
    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    public UUID getAssignedMachineId() { return assignedMachineId; }
    public void setAssignedMachineId(UUID assignedMachineId) { this.assignedMachineId = assignedMachineId; }
}