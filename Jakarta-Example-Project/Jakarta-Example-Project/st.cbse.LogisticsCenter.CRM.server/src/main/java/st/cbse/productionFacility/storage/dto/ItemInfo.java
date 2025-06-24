package st.cbse.productionFacility.storage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import st.cbse.productionFacility.storage.data.ItemData;

public class ItemInfo {

    private UUID id;
    private UUID orderId;
    private String name;
    private LocalDateTime createdAt;

    public ItemInfo() {
    }

    public ItemInfo(UUID id, UUID orderId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static ItemInfo fromEntity(ItemData data) {
        return new ItemInfo(
                data.getId(),
                data.getOrderId(),
                data.getName(),
                data.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}