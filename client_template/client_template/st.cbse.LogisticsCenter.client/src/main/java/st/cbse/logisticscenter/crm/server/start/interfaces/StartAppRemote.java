package st.cbse.logisticscenter.crm.server.start.interfaces;

import jakarta.ejb.Remote;
import st.cbse.logisticscenter.crm.server.start.data.LogMessage;

@Remote
public interface StartAppRemote {
	
	public LogMessage logMessage(String message);

}
