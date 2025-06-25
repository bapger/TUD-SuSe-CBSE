package st.cbse.productionFacility.storage.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class UnfinishedProduct {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private UUID processId;
    
    private UUID printRequestId;
    
    private String currentStepType;
    
    private UUID currentMachineId;
    
    @OneToOne(cascade = CascadeType.ALL)
    private ItemData itemData;
    
    protected UnfinishedProduct() {}
    
    public UnfinishedProduct(UUID processId, UUID printRequestId, ItemData itemData) {
        this.processId = processId;
        this.printRequestId = printRequestId;
        this.itemData = itemData;
    }
    
    public UUID getId() { return id; }
    public UUID getProcessId() { return processId; }
    public UUID getPrintRequestId() { return printRequestId; }
    public String getCurrentStepType() { return currentStepType; }
    public UUID getCurrentMachineId() { return currentMachineId; }
    public ItemData getItemData() { return itemData; }
    
    public void setCurrentStepType(String stepType) { this.currentStepType = stepType; }
    public void setCurrentMachineId(UUID machineId) { this.currentMachineId = machineId; }
}