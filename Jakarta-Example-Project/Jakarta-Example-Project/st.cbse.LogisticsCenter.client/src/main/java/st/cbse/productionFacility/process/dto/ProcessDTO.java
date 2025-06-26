package st.cbse.productionFacility.process.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class ProcessDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String status;
    private UUID printRequestId;
    private List<ProcessStepDTO> steps;
    private int currentStepIndex;
    private double progressPercentage;
    
    public ProcessDTO() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public UUID getPrintRequestId() { return printRequestId; }
    public void setPrintRequestId(UUID printRequestId) { this.printRequestId = printRequestId; }
    
    public List<ProcessStepDTO> getSteps() { return steps; }
    public void setSteps(List<ProcessStepDTO> steps) { this.steps = steps; }
    
    public int getCurrentStepIndex() { return currentStepIndex; }
    public void setCurrentStepIndex(int currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
}