package st.cbse.shipment.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.dto.ShipmentItemDTO;

@Remote
public interface IShipmentMgmt {

	void shipOrder(UUID orderId);

	List<ShipmentItemDTO> itemsInStorage();

}
