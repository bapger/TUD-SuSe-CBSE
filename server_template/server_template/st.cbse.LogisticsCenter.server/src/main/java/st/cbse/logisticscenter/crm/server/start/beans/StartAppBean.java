package st.cbse.logisticscenter.crm.server.start.beans;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import st.cbse.logisticscenter.crm.server.start.data.LogMessage;
import st.cbse.logisticscenter.crm.server.start.interfaces.StartAppRemote;

@Stateless
@LocalBean
public class StartAppBean implements StartAppRemote{

	@Override
	public LogMessage logMessage(String message) {
		return new LogMessage(message);
	}
    
}
