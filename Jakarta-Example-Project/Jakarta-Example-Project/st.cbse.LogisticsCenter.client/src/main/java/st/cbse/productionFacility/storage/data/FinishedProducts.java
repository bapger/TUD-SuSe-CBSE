package st.cbse.productionFacility.storage.data;

import java.time.LocalDateTime;
import java.util.UUID;

public class FinishedProducts {
    
    private UUID id = UUID.randomUUID();
    
    private UUID processId;
    
    private UUID printRequestId;
    
    private UUID orderId;
    
    private LocalDateTime completedAt = LocalDateTime.now();
    
    private boolean shipped = false;
    
    private ItemData itemData;
    
    protected FinishedProducts() {}
    
    public FinishedProducts(UUID processId, UUID printRequestId, ItemData itemData) {
        this.processId = processId;
        this.printRequestId = printRequestId;
        this.itemData = itemData;
    }
    
    public UUID getId() { return id; }
    public UUID getProcessId() { return processId; }
    public UUID getPrintRequestId() { return printRequestId; }
    public UUID getOrderId() { return orderId; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public boolean isShipped() { return shipped; }
    public ItemData getItemData() { return itemData; }
    
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public void setShipped(boolean shipped) { this.shipped = shipped; }
}