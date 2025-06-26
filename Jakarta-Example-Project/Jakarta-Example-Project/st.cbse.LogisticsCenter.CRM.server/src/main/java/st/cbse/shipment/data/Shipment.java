package st.cbse.shipment.data;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Shipment {
    @Id
    private UUID id = UUID.randomUUID();

    private Date shipmentDate;

    private UUID orderId;

    public Shipment() {
        this.shipmentDate = new Date();
    }

	public void setOrderId(UUID orderId2) {
		this.orderId = orderId2;	
	}
}
