package st.cbse.productionFacility.storage.beans;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import st.cbse.productionFacility.storage.data.*;
import st.cbse.productionFacility.storage.dto.FinishedProductsDto;
import st.cbse.productionFacility.storage.dto.ItemInfo;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;

@Singleton
@Startup
public class StorageBean implements IStorageMgmt {
    
    private static final Logger LOG = Logger.getLogger(StorageBean.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
    private UUID mainStorageId;
    
    @PostConstruct
    public void init() {
        Storage mainStorage = new Storage("Main Storage", 1000);
        em.persist(mainStorage);
        mainStorageId = mainStorage.getId();
        LOG.info("Initialized main storage with capacity 1000");
    }
    
    @Override
    public UUID createUnfinishedProduct(UUID processId, UUID printRequestId, String stlPath) {
        ItemData item = new ItemData(processId, printRequestId, stlPath);
        item.setStatus(ItemData.ItemStatus.IN_PRODUCTION);
        
        UnfinishedProduct unfinished = new UnfinishedProduct(processId, printRequestId, item);
        em.persist(unfinished);
        
        LOG.info("Created unfinished product for process " + processId);
        return item.getId();
    }
    
    @Override
    public boolean updateItemLocation(UUID itemId, String newLocation) {
        ItemData item = em.find(ItemData.class, itemId);
        if (item == null) return false;
        
        item.setCurrentLocation(newLocation);
        item.setStatus(ItemData.ItemStatus.IN_TRANSIT);
        em.merge(item);
        return true;
    }
    
    @Override
    public boolean convertToFinished(UUID processId) {
        UnfinishedProduct unfinished = em.createQuery(
                "SELECT u FROM UnfinishedProduct u WHERE u.processId = :processId", 
                UnfinishedProduct.class)
                .setParameter("processId", processId)
                .getResultStream()
                .findFirst()
                .orElse(null);
                
        if (unfinished == null) return false;
        
        ItemData item = unfinished.getItemData();
        item.setStatus(ItemData.ItemStatus.IN_STORAGE);
        
        unfinished.setItemData(null);
        em.merge(unfinished);
        
        FinishedProducts finished = new FinishedProducts(
                processId, 
                unfinished.getPrintRequestId(), 
                item
        );
        
        Storage storage = em.find(Storage.class, mainStorageId);
        if (storage.addItem(item)) {
            em.persist(finished);
            em.remove(unfinished);
            em.merge(storage);
            
            LOG.info("Converted process " + processId + " to finished product");
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<FinishedProductsDto> getAllFinishedProducts() {
        List<FinishedProducts> products = em.createQuery(
                "SELECT f FROM FinishedProducts f", FinishedProducts.class)
                .getResultList();
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FinishedProductsDto> getFinishedProductsByOrder(UUID orderId) {
        List<FinishedProducts> products = em.createQuery(
                "SELECT f FROM FinishedProducts f WHERE f.orderId = :orderId", 
                FinishedProducts.class)
                .setParameter("orderId", orderId)
                .getResultList();
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FinishedProductsDto> getUnshippedProducts() {
        List<FinishedProducts> products = em.createQuery(
                "SELECT f FROM FinishedProducts f WHERE f.shipped = false", 
                FinishedProducts.class)
                .getResultList();
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ItemInfo getItemInfo(UUID itemId) {
        ItemData item = em.find(ItemData.class, itemId);
        return item != null ? toItemInfo(item) : null;
    }
    
    @Override
    public boolean markAsShipped(UUID finishedProductId) {
        FinishedProducts product = em.find(FinishedProducts.class, finishedProductId);
        if (product == null) return false;
        
        product.setShipped(true);
        product.getItemData().setStatus(ItemData.ItemStatus.SHIPPED);
        em.merge(product);
        
        Storage storage = em.find(Storage.class, mainStorageId);
        storage.removeItem(product.getItemData());
        em.merge(storage);
        
        return true;
    }
    
    @Override
    public boolean markOrderAsShipped(UUID orderId) {
        List<FinishedProducts> products = em.createQuery(
                "SELECT f FROM FinishedProducts f WHERE f.orderId = :orderId AND f.shipped = false", 
                FinishedProducts.class)
                .setParameter("orderId", orderId)
                .getResultList();
                
        for (FinishedProducts product : products) {
            markAsShipped(product.getId());
        }
        
        LOG.info("Marked " + products.size() + " products as shipped for order " + orderId);
        return !products.isEmpty();
    }
    
    @Override
    public boolean transportItem(UUID itemId) {
        return updateItemLocation(itemId, "In Transit");
    }
    
    @Override
    public int getStorageCapacity() {
        Storage storage = em.find(Storage.class, mainStorageId);
        return storage != null ? storage.getCapacity() : 0;
    }
    
    @Override
    public int getCurrentOccupancy() {
        Storage storage = em.find(Storage.class, mainStorageId);
        return storage != null ? storage.getCurrentOccupancy() : 0;
    }
    
    @Override
    public boolean hasStorageSpace() {
        Storage storage = em.find(Storage.class, mainStorageId);
        return storage != null && storage.hasSpace();
    }
    private FinishedProductsDto toDTO(FinishedProducts product) {
        FinishedProductsDto dto = new FinishedProductsDto();
        dto.setId(product.getId());
        dto.setProcessId(product.getProcessId());
        dto.setPrintRequestId(product.getPrintRequestId());
        dto.setOrderId(product.getOrderId());
        dto.setCompletedAt(product.getCompletedAt());
        dto.setShipped(product.isShipped());
        dto.setItemInfo(toItemInfo(product.getItemData()));
        return dto;
    }
    
    private ItemInfo toItemInfo(ItemData item) {
        ItemInfo info = new ItemInfo();
        info.setId(item.getId());
        info.setProcessId(item.getProcessId());
        info.setPrintRequestId(item.getPrintRequestId());
        info.setCurrentLocation(item.getCurrentLocation());
        info.setStatus(item.getStatus().name());
        info.setCreatedAt(item.getCreatedAt());
        return info;
    }
}