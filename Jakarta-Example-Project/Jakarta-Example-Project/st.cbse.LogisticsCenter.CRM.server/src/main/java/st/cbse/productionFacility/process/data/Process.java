package st.cbse.productionFacility.process.data;

import jakarta.persistence.*;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;

import java.util.*;

@Entity
public class Process {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Enumerated(EnumType.STRING)
    private ProcessStatus status = ProcessStatus.CREATED;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stepOrder ASC")
    private List<ProcessStep> steps = new ArrayList<>();
    
    private UUID printRequestId;
    
    private UUID orderId;
    
    private int currentStepIndex = 0;
    
    protected Process() {}
    
    public Process(UUID printRequestId) {
        this.printRequestId = printRequestId;
    }
    
    public UUID getId() { return id; }
    public ProcessStatus getStatus() { return status; }
    public List<ProcessStep> getSteps() { return new ArrayList<>(steps); }
    public UUID getPrintRequestId() { return printRequestId; }
    public int getCurrentStepIndex() { return currentStepIndex; }
    
    public void setStatus(ProcessStatus status) { this.status = status; }
    
    public void setSteps(List<ProcessStep> steps) {
        this.steps.clear();
        this.steps.addAll(steps);
    }
    
    public void addStep(ProcessStep step) {
        this.steps.add(step);
    }
    
    public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setPrintRequestId(UUID printRequestId) {
		this.printRequestId = printRequestId;
	}

	public void setCurrentStepIndex(int currentStepIndex) {
		this.currentStepIndex = currentStepIndex;
	}

	public ProcessStep getCurrentStep() {
        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
            return steps.get(currentStepIndex);
        }
        return null;
    }
    
    public boolean moveToNextStep() {
        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            return true;
        }
        return false;
    }
    
    public boolean isAllStepsCompleted() {
        return steps.stream().allMatch(ProcessStep::isCompleted);
    }
}