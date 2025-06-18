package st.cbse.crm.data;

import jakarta.persistence.*;
import st.cbse.crm.data.enums.OrderStatus;

import java.util.*;

@Entity
public class Order {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private boolean paid;
    private double amount;
    private Date creationDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<PrintingRequest> printingRequests = new ArrayList<>();

    public UUID getId() { return id; }

	public void setCustomer(Customer customer2) {
		// TODO Auto-generated method stub
		
	}

	public void setCreationDate(Date date) {
		// TODO Auto-generated method stub
		
	}

	public void setOrderStatus(OrderStatus created) {
		// TODO Auto-generated method stub
		
	}

	public void setPaid(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setPrintingRequests(List<PrintingRequest> requests) {
		// TODO Auto-generated method stub
		
	}
}
