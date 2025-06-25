package st.cbse.productionFacility.storage.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import st.cbse.productionFacility.storage.data.UnfinishedProduct;
import st.cbse.productionFacility.storage.data.ItemData;
import st.cbse.productionFacility.storage.data.Storage;
import st.cbse.productionFacility.storage.dto.ItemInfo;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;

@Stateless
public class StorageBean implements IStorageMgmt {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ItemInfo> viewItems() {
        return loadStorage().getFinishedProducts()
                            .stream()
                            .map(ItemInfo::fromEntity)
                            .collect(Collectors.toList());
    }

    @Override
    public boolean addItem(ItemData itemData) {
        Storage storage = loadStorage();
        storage.addItem(itemData);
        em.persist(itemData);
        em.merge(storage);
        return true;
    }

    @Override
    public boolean removeItem(UUID itemId) {
        Storage storage = loadStorage();
        ItemData item = em.find(ItemData.class, itemId);
        if (item == null) {
            return false;
        }
        storage.removeItem(item);
        em.remove(item);
        em.merge(storage);
        return true;
    }

    @Override
    public UUID finishItem(UnfinishedProduct unfinished, UUID orderId, String name) {
        ItemData finished = new ItemData(orderId, unfinished.getProcessId(), name);
        addItem(finished);
        return finished.getId();
    }

    private Storage loadStorage() {
        return em.createQuery("SELECT s FROM Storage s", Storage.class)
                 .setMaxResults(1)
                 .getResultStream()
                 .findFirst()
                 .orElseGet(this::createStorage);
    }

    private Storage createStorage() {
        Storage s = new Storage();
        em.persist(s);
        return s;
    }
}