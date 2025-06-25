package st.cbse.crm.managerComponent.interfaces;

import java.util.UUID;

import jakarta.ejb.Remote;

@Remote
public interface IManagerMgmt {
	void initManager();

	UUID loginManager(String email, String password);

}
