package st.cbse.productionFacility.storage.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class FinishedProductsDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private UUID processId;
    private UUID printRequestId;
    private UUID orderId;
    private LocalDateTime completedAt;
    private boolean shipped;
    private ItemInfo itemInfo;
    
    public FinishedProductsDto() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getProcessId() { return processId; }
    public void setProcessId(UUID processId) { this.processId = processId; }
    
    public UUID getPrintRequestId() { return printRequestId; }
    public void setPrintRequestId(UUID printRequestId) { this.printRequestId = printRequestId; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public boolean isShipped() { return shipped; }
    public void setShipped(boolean shipped) { this.shipped = shipped; }
    
    public ItemInfo getItemInfo() { return itemInfo; }
    public void setItemInfo(ItemInfo itemInfo) { this.itemInfo = itemInfo; }
}