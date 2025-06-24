package st.cbse.productionFacility.storage.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storage")
public class Storage {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "storage_id")
    private List<ItemData> finishedProducts = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public List<ItemData> getFinishedProducts() {
        return Collections.unmodifiableList(finishedProducts);
    }

    public void addItem(ItemData item) {
        finishedProducts.add(item);
    }

    public void removeItem(ItemData item) {
        finishedProducts.remove(item);
    }
}