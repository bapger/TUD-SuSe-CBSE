package st.cbse.logisticscenter.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import st.cbse.logisticscenter.crm.server.start.data.LogMessage;
import st.cbse.logisticscenter.crm.server.start.interfaces.StartAppRemote;

public class Client {

	public static void main(String[] args) {
		
		try {
			System.out.println("Connect to Server ...");
			InitialContext context = getContext();
			StartAppRemote bean = lookupBean(context);
			
			LogMessage msg = bean.logMessage("This is a Test.");
			
			System.out.println(msg.getLogMessage());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Lookup method for the server context
	 */
	public static InitialContext getContext() throws NamingException {
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory"); 
		props.put(Context.PROVIDER_URL,"http-remoting://localhost:8080");
		return new InitialContext(props);
	}
	
	/**
	 * Lookup method for the bean
	 */
	public static StartAppRemote lookupBean(InitialContext context) throws NamingException {
		
		// See JNDI names given at server start
		String name = "ejb:/st.cbse.LogisticsCenter.CRM.server/StartAppBean!st.cbse.logisticscenter.crm.server.start.interfaces.StartAppRemote";
		return (StartAppRemote) context.lookup(name);
	}
}
