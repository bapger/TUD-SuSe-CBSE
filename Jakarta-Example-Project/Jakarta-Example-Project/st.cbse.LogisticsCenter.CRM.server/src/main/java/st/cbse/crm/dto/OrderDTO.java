package st.cbse.crm.dto;

import st.cbse.crm.orderComponent.data.Order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID                  id;
    private final String                status;
    private final String                customerName;
    private final LocalDateTime         creationDate;
    private final BigDecimal            total;
    private final List<PrintRequestDTO>	printingRequests;

    private OrderDTO(UUID id,
                     String status,
                     String customerName,
                     LocalDateTime creationDate,
                     BigDecimal total,
                     List<PrintRequestDTO> requests) {

        this.id          		= id;
        this.status           	= status;
        this.customerName     	= customerName;
        this.creationDate     	= creationDate;
        this.total            	= total;
        this.printingRequests 	= List.copyOf(requests);
    }

    public static OrderDTO of(Order o) {
        List<PrintRequestDTO> reqDtos = o.getPrintingRequests()
                                         .stream()
                                         .map(PrintRequestDTO::of)
                                         .collect(Collectors.toList());

        return new OrderDTO(o.getId(),
                            o.getOrderStatus().name(),
                            o.getCustomerId().toString(),
                            o.getCreationDate(),
                            o.getTotal(),
                            reqDtos);
    }

    public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCustomerName() {
		return customerName;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public List<PrintRequestDTO> getPrintingRequests() {
		return printingRequests;
	}

    public UUID getId()      		{ return id; }
    public String getStatus()       { return status; }
    public String getCustomer()		{return customerName;}
    public BigDecimal getTotal()	{return total;}
}