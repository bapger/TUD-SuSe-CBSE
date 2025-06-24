package st.cbse.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import st.cbse.crm.orderComponent.data.*;

/**
 * Top-level DTO sent to both customers (“history”) and managers
 * (“list all orders”).  Converts the entire Order entity graph into
 * a tree of immutable value objects.
 */
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID                  id;
    private final String                customerName;   // convenient for managers
    private final OrderStatus           status;
    private final BigDecimal            total;
    private final LocalDateTime         creationDate;
    private final List<PrintRequestDTO> printingRequests;

    /* Factory converts an Order entity into a DTO in one pass */
    public static OrderDTO of(Order o) {
        List<PrintRequestDTO> prDTOs = o.getPrintingRequests()
                                        .stream()
                                        .map(pr -> new PrintRequestDTO(
                                                pr.getId(),
                                                pr.getStlPath(),
                                                pr.getNote(),
                                                ((List<PrintRequestDTO>) pr.getOptions())
                                                  .stream()
                                                  .map(op -> new OptionDTO(
                                                          op.getClass().getSimpleName(),
                                                          op.getPrice()))
                                                  .toList()))
                                        .toList();

        return new OrderDTO(o.getId(),
                            o.getCustomer().getName(),
                            o.getOrderStatus(),
                            o.getTotal(),
                            o.getCreationDate(),
                            prDTOs);
    }

    /* private ctor enforces usage of the factory */
    private OrderDTO(UUID id,
                     String customerName,
                     OrderStatus status,
                     BigDecimal total,
                     LocalDateTime creationDate,
                     List<PrintRequestDTO> printingRequests) {
        this.id               = id;
        this.customerName     = customerName;
        this.status           = status;
        this.total            = total;
        this.creationDate     = creationDate;
        this.printingRequests = List.copyOf(printingRequests);
    }

    /* getters */
    public UUID getId()                        { return id; }
    public String getCustomerName()            { return customerName; }
    public OrderStatus getStatus()             { return status; }
    public BigDecimal getTotal()               { return total; }
    public LocalDateTime getCreationDate()     { return creationDate; }
    public List<PrintRequestDTO> getPrintingRequests() { return printingRequests; }
}