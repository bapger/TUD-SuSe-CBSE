package st.cbse.crm.managerComponent.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;

@Remote
public interface IManagerMgmt {
	void initManager();

	String loginManager(String email, String password);
	List<UUID> sendPrintToProd(UUID orderId);
}
