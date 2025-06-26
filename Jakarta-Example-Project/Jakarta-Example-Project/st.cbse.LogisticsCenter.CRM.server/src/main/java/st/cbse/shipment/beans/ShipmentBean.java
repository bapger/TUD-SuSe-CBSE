package st.cbse.shipment.beans;

import jakarta.ejb.Stateless;
import jakarta.ejb.EJB;
import jakarta.persistence.*;
import java.util.UUID;
import java.util.logging.Logger;

import st.cbse.shipment.data.*;
import st.cbse.shipment.interfaces.IShipmentMgmt;
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
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        em.persist(shipment);

        storageMgmt.markOrderAsShipped(orderId);
        
        LOG.info("Shipped order: " + orderId);
    }
}