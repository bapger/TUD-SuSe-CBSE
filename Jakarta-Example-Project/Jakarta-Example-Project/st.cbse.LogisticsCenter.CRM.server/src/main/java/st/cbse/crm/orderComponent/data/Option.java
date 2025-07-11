package st.cbse.crm.orderComponent.data;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "option_type")
public abstract class Option {
    @Id
    protected UUID id = UUID.randomUUID();
    
    @ManyToOne
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
