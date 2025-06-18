package st.cbse.shipment.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.UUID;
import st.cbse.shipment.data.*;
import st.cbse.shipment.interfaces.IShipmentMgmt;

@Stateless
class ShipmentBean implements IShipmentMgmt {
    @PersistenceContext
    private EntityManager em;

    public void shipOrder(UUID orderId) {
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        em.persist(shipment);
    }
}
