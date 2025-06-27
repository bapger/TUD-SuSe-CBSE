package st.cbse.productionFacility.storage.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class ItemInfo implements Serializable {
    
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5936628697602512457L;
	private UUID id;
    private UUID processId;
    private UUID printRequestId;
    private String currentLocation;
    private String status;
    private LocalDateTime createdAt;
    
    public ItemInfo() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getProcessId() { return processId; }
    public void setProcessId(UUID processId) { this.processId = processId; }
    
    public UUID getPrintRequestId() { return printRequestId; }
    public void setPrintRequestId(UUID printRequestId) { this.printRequestId = printRequestId; }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}