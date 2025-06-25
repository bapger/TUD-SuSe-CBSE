package st.cbse.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import st.cbse.crm.orderComponent.data.Option;

/**
 * Smallest item in the DTO graph: represents one extra option
 * (paint job, smoothing, engraving, …) that was chosen for a
 * print request.
 */
public class OptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String     type;    // simple class name, e.g. "PaintJob"
    private final BigDecimal price;   // unit price that was charged

    public OptionDTO(String type, BigDecimal price) {
        this.type  = type;
        this.price = price;
    }

    /* getters only (immutability) */
    public String getType()      { return type; }
    public BigDecimal getPrice() { return price; }
    
    public static OptionDTO of(Option o) {
        return new OptionDTO(o.getClass().getSimpleName(), o.getPrice());
    }
}