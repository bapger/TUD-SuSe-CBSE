package st.cbse.productionFacility.storage.data;

import java.util.UUID;

public class UnfinishedProduct {

    private final UUID id = UUID.randomUUID();
    private final UUID processId;

    public UnfinishedProduct(UUID processId) {
        this.processId = processId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProcessId() {
        return processId;
    }
}