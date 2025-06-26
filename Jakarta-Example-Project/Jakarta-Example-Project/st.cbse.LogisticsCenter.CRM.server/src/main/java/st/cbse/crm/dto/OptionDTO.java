package st.cbse.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import st.cbse.crm.orderComponent.data.Option;

public class OptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String  		type;
    private final BigDecimal 	price;

    public OptionDTO(String type, BigDecimal price) {
        this.type  = type;
        this.price = price;
    }

    public String getType()      { return type; }
    public BigDecimal getPrice() { return price; }
    
    public static OptionDTO of(Option o) {
        return new OptionDTO(o.getClass().getSimpleName(), o.getPrice());
    }
}