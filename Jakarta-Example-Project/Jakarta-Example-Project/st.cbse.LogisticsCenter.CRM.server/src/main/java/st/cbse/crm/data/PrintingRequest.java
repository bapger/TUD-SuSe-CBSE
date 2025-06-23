package st.cbse.crm.data;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class PrintingRequest {
    @Id
    private UUID id = UUID.randomUUID();
    private String description;

    @ManyToOne
    private Order order;

    @OneToMany(mappedBy = "printingRequest", cascade = CascadeType.ALL)
    private List<Option> options = new ArrayList<>();


    public PrintingRequest() {}

	public void setDescription(String description2) {
		this.description = description2;
		
	}

	public void setOrder(Order order2) {
		this.order = order2;
		
	}

	public UUID getId() {
		// TODO Auto-generated method stub
		return id;
	}

	public void addOption(Option option) {
		options.add(option);
		
	}
}
