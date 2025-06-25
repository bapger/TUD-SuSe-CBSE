package st.cbse.productionFacility.storage.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.productionFacility.storage.data.UnfinishedProduct;
import st.cbse.productionFacility.storage.data.ItemData;
import st.cbse.productionFacility.storage.dto.ItemInfo;

@Remote
public interface IStorageMgmt {

    List<ItemInfo> viewItems();
    boolean addItem(ItemData itemData);
    boolean removeItem(UUID itemId);
    UUID finishItem(UnfinishedProduct unfinished, UUID orderId, String name);
}