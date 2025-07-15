package st.cbse.productionFacility.storage.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class FinishedProducts {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private UUID processId;
    
    private UUID printRequestId;
    
    public void setId(UUID id) {
		this.id = id;
	}

	public void setProcessId(UUID processId) {
		this.processId = processId;
	}

	public void setPrintRequestId(UUID printRequestId) {
		this.printRequestId = printRequestId;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public void setItemData(ItemData itemData) {
		this.itemData = itemData;
	}
	private UUID orderId;
    
    private LocalDateTime completedAt = LocalDateTime.now();
    
    private boolean shipped = false;
    
    @OneToOne(cascade = CascadeType.ALL)
    private ItemData itemData;
    
    protected FinishedProducts() {}
    
    public FinishedProducts(UUID processId, UUID printRequestId, ItemData itemData,UUID orderId) {
        this.processId = processId;
        this.printRequestId = printRequestId;
        this.itemData = itemData;
        this.orderId = orderId;
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