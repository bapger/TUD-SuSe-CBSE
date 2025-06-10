package st.cbse.logisticscenter.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import st.cbse.calculator.data.Result;
import st.cbse.calculator.interfaces.SimpleCalculatorRemote;

public class Client {

	public static void main(String[] args) {
		
		try {
			System.out.println("Start the calculator!");
			   
			InitialContext context = getContext();
			SimpleCalculatorRemote bean = lookupBean(context);
			
			Result r = bean.result();
			
			System.out.println("Check former calculations!");
			if(r!=null) {System.out.println(r.getSequence() + " = " + r.getValue());}else {System.out.println("No former Result in Database.");}
			
			System.out.println("Run the first calculation!");
	        r = bean.start(10);
	        System.out.println(r.getSequence() + " = " + r.getValue());
	        r = bean.add(10);
	        System.out.println(r.getSequence() + " = " + r.getValue());
	        r = bean.sub(5);
	        System.out.println(r.getSequence() + " = " + r.getValue());
	        r = bean.result();
	        System.out.println(r.getSequence() + " = " + r.getValue());
	        System.out.println("Run the second calculation!");
	        r = bean.start(20);
	        System.out.println(r.getSequence() + " = " + r.getValue());
	        r = bean.add(10);
	        System.out.println(r.getSequence() + " = " + r.getValue());
			
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
	public static SimpleCalculatorRemote lookupBean(InitialContext context) throws NamingException {
		
		// See JNDI names given at server start
		String name = "ejb:/st.cbse.LogisticsCenter.CRM.server/SimpleCalculator!st.cbse.calculator.interfaces.SimpleCalculatorRemote";
		return (SimpleCalculatorRemote) context.lookup(name);
	}
}
