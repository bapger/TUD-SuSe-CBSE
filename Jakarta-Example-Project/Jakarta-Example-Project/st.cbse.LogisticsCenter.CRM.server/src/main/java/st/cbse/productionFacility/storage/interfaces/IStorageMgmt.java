package st.cbse.productionFacility.storage.interfaces;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.storage.dto.FinishedProductsDto;
import st.cbse.productionFacility.storage.dto.ItemInfo;

@Local
public interface IStorageMgmt {
    
    UUID createUnfinishedProduct(UUID processId, UUID printRequestId, String stlPath);
    
    boolean updateItemLocation(UUID itemId, String newLocation);
    
    boolean convertToFinished(UUID processId);
    
    List<FinishedProductsDto> getAllFinishedProducts();
    List<FinishedProductsDto> getFinishedProductsByOrder(UUID orderId);
    List<FinishedProductsDto> getUnshippedProducts();
    
    ItemInfo getItemInfo(UUID itemId);
    
    boolean markAsShipped(UUID finishedProductId);
    boolean markOrderAsShipped(UUID orderId);
    
    boolean transportItem(UUID itemId);
    
    int getStorageCapacity();
    int getCurrentOccupancy();
    boolean hasStorageSpace();
}