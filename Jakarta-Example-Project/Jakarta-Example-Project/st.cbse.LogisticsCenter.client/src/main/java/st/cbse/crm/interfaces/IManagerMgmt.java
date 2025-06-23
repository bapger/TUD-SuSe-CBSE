package st.cbse.crm.interfaces;

import java.util.UUID;

public interface IManagerMgmt {

	UUID loginManager(String email3, String password3);

	void addNoteToRequest(UUID reqId, String note);

	void markRequestFinished(UUID reqId);

}
