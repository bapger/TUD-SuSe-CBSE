package st.cbse.productionFacility.storage.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
public class ItemData {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ItemData() {
    }

    public ItemData(UUID processId, UUID orderId, String name) {
        this.processId = processId;
        this.orderId = orderId;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProcessId() {
        return processId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}