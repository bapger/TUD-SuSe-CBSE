package st.cbse.crm.shipment.interfaces;


import java.util.UUID;

import jakarta.ejb.Remote;

@Remote
public interface IShipmentMgmt {
    void shipOrder(UUID orderId);
}
