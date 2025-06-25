package st.cbse.crm.shipmentComponent.interfaces;

import java.util.List;

import st.cbse.crm.dto.ShipmentItemDTO;

public interface IShipmentMgmt {

	List<ShipmentItemDTO> itemsInStorage();

}
