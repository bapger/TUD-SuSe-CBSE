package st.cbse.productionFacility.process.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class ProcessStep {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private String stepType;
    
    private int stepOrder;
    
    private boolean completed = false;
    
    private UUID assignedMachineId;
    
    protected ProcessStep() {}
    
    public ProcessStep(String stepType, int stepOrder) {
        this.stepType = stepType;
        this.stepOrder = stepOrder;
    }
    
    public UUID getId() { return id; }
    public String getStepType() { return stepType; }
    public int getStepOrder() { return stepOrder; }
    public boolean isCompleted() { return completed; }
    public UUID getAssignedMachineId() { return assignedMachineId; }
    
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setAssignedMachineId(UUID machineId) { this.assignedMachineId = machineId; }
}