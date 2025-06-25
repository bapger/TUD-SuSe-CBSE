package st.cbse.crm.orderComponent.data;

import jakarta.persistence.Entity;

@Entity
public class Smoothing extends Option {

	private String granularity;
	protected Smoothing() {}
	public Smoothing(String granularity) {
		this.granularity = granularity;
	}

}
