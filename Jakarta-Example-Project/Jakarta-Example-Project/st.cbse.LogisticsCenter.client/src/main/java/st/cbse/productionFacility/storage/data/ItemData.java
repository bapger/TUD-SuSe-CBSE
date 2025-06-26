package st.cbse.productionFacility.storage.data;

import java.time.LocalDateTime;
import java.util.UUID;

public class ItemData {
    
    private UUID id = UUID.randomUUID();
    
    private UUID processId;
    
    private UUID printRequestId;
    
    private String stlPath;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String currentLocation;
    
    private ItemStatus status = ItemStatus.IN_PRODUCTION;
    
    protected ItemData() {}
    
    public ItemData(UUID processId, UUID printRequestId, String stlPath) {
        this.processId = processId;
        this.printRequestId = printRequestId;
        this.stlPath = stlPath;
    }
    
    public UUID getId() { return id; }
    public UUID getProcessId() { return processId; }
    public UUID getPrintRequestId() { return printRequestId; }
    public String getStlPath() { return stlPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCurrentLocation() { return currentLocation; }
    public ItemStatus getStatus() { return status; }
    
    public void setCurrentLocation(String location) { this.currentLocation = location; }
    public void setStatus(ItemStatus status) { this.status = status; }
    
    public enum ItemStatus {
        IN_PRODUCTION,
        IN_TRANSIT,
        IN_STORAGE,
        SHIPPED
    }
}