package st.cbse.crm.orderComponent.data;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


public class Order {


    private UUID id = UUID.randomUUID();

    private UUID customerId;

    private List<PrintingRequest> printingRequests = new ArrayList<>();

    private Invoice invoice;


    private OrderStatus orderStatus;

    private BigDecimal basePrice = BigDecimal.ZERO;
    private BigDecimal total     = BigDecimal.ZERO;

    private LocalDateTime creationDate;
    private boolean      paid;

    public UUID getId()                         { return id; }

    public UUID getCustomerId()                 { return customerId; }
    public void setCustomerId(UUID customerId)  { this.customerId = customerId; }

    public OrderStatus getOrderStatus()               { return orderStatus; }
    public void setOrderStatus(OrderStatus status)    { this.orderStatus = status; }

    public BigDecimal getBasePrice()                  { return basePrice; }
    public void setBasePrice(BigDecimal basePrice)    { this.basePrice = basePrice; }

    public BigDecimal getTotal()                      { return total; }
    public void setTotal(BigDecimal total)            { this.total = total; }

    public LocalDateTime getCreationDate()            { return creationDate; }
    public void setCreationDate(LocalDateTime date)   { this.creationDate = date; }

    public boolean isPaid()                           { return paid; }
    public void setPaid(boolean paid)                 { this.paid = paid; }

    public List<PrintingRequest> getPrintingRequests(){ return printingRequests; }
    public void setPrintingRequests(List<PrintingRequest> pr)
                                                     { this.printingRequests = pr; }

    public Invoice getInvoice()                       { return invoice; }
    public void setInvoice(Invoice invoice)           { this.invoice = invoice; }
}