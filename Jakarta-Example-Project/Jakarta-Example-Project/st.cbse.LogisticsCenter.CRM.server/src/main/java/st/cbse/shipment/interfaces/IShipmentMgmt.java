package st.cbse.shipment.interfaces;

import jakarta.ejb.Remote;
import java.util.UUID;

@Remote
public interface IShipmentMgmt {
    void shipOrder(UUID orderId);
}
