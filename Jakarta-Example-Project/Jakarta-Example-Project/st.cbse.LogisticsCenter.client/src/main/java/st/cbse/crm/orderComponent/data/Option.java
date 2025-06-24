package st.cbse.crm.orderComponent.data;


import java.math.BigDecimal;
import java.util.UUID;

public abstract class Option {
    protected UUID id = UUID.randomUUID();
    private PrintingRequest printingRequest;


    protected String name;
    protected String description;
    private BigDecimal price;

    public UUID getId() {
        return id;
    }

	public void setPrice(BigDecimal unitPrice) {
		this.price = unitPrice;
		
	}

	public void setPrintingRequest(PrintingRequest pr) {
		this.printingRequest = pr;
		
	}

	public BigDecimal getPrice() {
		return price;
	}
}
