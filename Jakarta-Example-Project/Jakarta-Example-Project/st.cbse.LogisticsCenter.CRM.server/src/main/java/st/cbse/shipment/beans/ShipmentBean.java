package st.cbse.shipment.beans;

import jakarta.ejb.Stateless;
import jakarta.ejb.EJB;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import st.cbse.shipment.data.*;
import st.cbse.shipment.interfaces.IShipmentMgmt;
import st.cbse.productionFacility.storage.dto.FinishedProductsDto;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;

@Stateless
public class ShipmentBean implements IShipmentMgmt {
    
    private static final Logger LOG = Logger.getLogger(ShipmentBean.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private IStorageMgmt storageMgmt;

    @Override
    public void shipOrder(UUID orderId) {
        // Persist a shipment record
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        em.persist(shipment);

        // Fetch finished products for the order using the interface
        List<FinishedProductsDto> products = storageMgmt.getFinishedProductsByOrder(orderId);

        // Mark each product as shipped using the existing interface method
        for (FinishedProductsDto product : products) {
            storageMgmt.markAsShipped(product.getId());  // mark and remove from storage
            LOG.warning("Shipped product: " + product.getId() + " isShipped: "+ product.isShipped());
        }

        LOG.info("Shipped order: " + orderId);
    }

}