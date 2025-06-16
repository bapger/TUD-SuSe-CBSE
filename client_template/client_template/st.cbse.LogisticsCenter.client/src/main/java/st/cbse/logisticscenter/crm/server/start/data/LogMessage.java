package st.cbse.logisticscenter.crm.server.start.data;

import java.io.Serializable;

public class LogMessage implements Serializable {
	// for en- and decoding objects during transportations
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public String getLogMessage() {
		return this.message;
	}

}
