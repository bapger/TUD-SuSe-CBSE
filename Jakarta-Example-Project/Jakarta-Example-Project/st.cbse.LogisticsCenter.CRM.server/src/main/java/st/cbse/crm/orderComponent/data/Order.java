package st.cbse.crm.orderComponent.data;

import jakarta.persistence.*;
import st.cbse.crm.customerComponent.data.Customer;
import st.cbse.crm.orderComponent.data.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

	public void setCreationDate(LocalDateTime localDateTime) {
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

	public List<PrintingRequest> getPrintingRequests() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBasePrice(BigDecimal basePrice) {
		// TODO Auto-generated method stub
		
	}

	public void setTotal(BigDecimal basePrice) {
		// TODO Auto-generated method stub
		
	}

	public OrderStatus getOrderStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getTotal() {
		// TODO Auto-generated method stub
		return null;
	}

	public Invoice getInvoice() {
		// TODO Auto-generated method stub
		return null;
	}
}
