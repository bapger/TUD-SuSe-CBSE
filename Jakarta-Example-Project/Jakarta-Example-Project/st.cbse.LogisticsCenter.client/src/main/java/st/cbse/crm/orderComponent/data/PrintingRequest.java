package st.cbse.crm.orderComponent.data;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import st.cbse.crm.dto.OptionDTO;

public class PrintingRequest {

    private UUID id = UUID.randomUUID();

    private String description;

    private String stlPath;          

    private String note;              


    private Order order;

    private List<Option> options = new ArrayList<>();


    public PrintingRequest() { }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setStlPath(String stlPath) {
        this.stlPath = stlPath;
    }

    public void setNote(String note) {
        this.note = note;
    }


    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public String getStlPath() {
        return stlPath;
    }

    public String getNote() {
        return note;
    }


    public BigDecimal getOptionsPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (Option o : options) {
            if (o.getPrice() != null) {
                total = total.add(o.getPrice());
            }
        }
        return total;
    }

    public void addOption(Option option) {
        option.setPrintingRequest(this);
        options.add(option);
    }

    public void add(Option option) {
        addOption(option);
    }

	public List<Option> getOptions() {
		return options;
	}
}