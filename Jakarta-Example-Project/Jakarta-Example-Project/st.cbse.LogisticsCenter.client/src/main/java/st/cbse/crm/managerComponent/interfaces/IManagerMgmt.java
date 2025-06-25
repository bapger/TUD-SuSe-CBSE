package st.cbse.crm.managerComponent.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.dto.OrderDTO;

@Remote
public interface IManagerMgmt {
	void initManager();

	String loginManager(String email, String pw);

	void addNoteToRequest(UUID reqId, String note);

	void markRequestFinished(UUID reqId);

	List<OrderDTO> listAllOrders();

	void sendPrintToProd(UUID reqId);

}
