package st.cbse.calculator.data;

import java.io.Serializable;

public class Result implements Serializable{

	private static final long serialVersionUID = 7422574264557894633L;
	
	private Long id;
	private Integer value;
	private String sequence;
	
	public Integer getValue() {
		return value;
	}

	public String getSequence() {
		return sequence;
	}

}

