package st.cbse.productionFacility.production.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Transport {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private UUID itemId;
    private UUID processId;
    private UUID fromMachineId;
    private UUID toMachineId;
    
    @Enumerated(EnumType.STRING)
    private TransportStatus status = TransportStatus.PENDING;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    protected Transport() {}
    
    public Transport(UUID itemId, UUID processId, UUID fromMachineId, UUID toMachineId) {
        this.itemId = itemId;
        this.processId = processId;
        this.fromMachineId = fromMachineId;
        this.toMachineId = toMachineId;
    }
    
    public UUID getId() { return id; }
    public UUID getItemId() { return itemId; }
    public UUID getProcessId() { return processId; }
    public UUID getFromMachineId() { return fromMachineId; }
    public UUID getToMachineId() { return toMachineId; }
    public TransportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setStatus(TransportStatus status) { this.status = status; }
    
    public enum TransportStatus {
        PENDING,
        IN_TRANSIT,
        DELIVERED
    }
}