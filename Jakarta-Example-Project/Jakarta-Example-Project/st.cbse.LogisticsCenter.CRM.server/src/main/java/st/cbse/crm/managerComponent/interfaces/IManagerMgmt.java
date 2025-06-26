package st.cbse.crm.managerComponent.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.dto.OrderDTO;

@Remote
public interface IManagerMgmt {
	

	String loginManager(String email, String password);
	List<UUID> sendPrintToProd(UUID orderId);

	void addNoteToRequest(UUID requestId, String note);

	List<OrderDTO> listAllOrders();
}
