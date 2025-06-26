package st.cbse.crm.orderComponent.data;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Invoice {

    /* ================== clés & méta-données ================== */

    @Id
    private UUID id = UUID.randomUUID();

    private LocalDateTime issueDate = LocalDateTime.now();


    private BigDecimal amount = BigDecimal.ZERO;

    private String paymentRef;

    private LocalDateTime paidDate;


    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;


    public Invoice() { }

    public Invoice(Order order, BigDecimal amount) {
        this.order  = order;
        this.amount = amount;
    }


    public UUID getId()                         { return id; }

    public LocalDateTime getIssueDate()         { return issueDate; }
    public void setIssueDate(LocalDateTime d)   { this.issueDate = d; }

    public BigDecimal getAmount()               { return amount; }
    public void setAmount(BigDecimal amount)    { this.amount = amount; }

    public String getPaymentRef()               { return paymentRef; }
    public LocalDateTime getPaidDate()          { return paidDate; }

    public Order getOrder()                     { return order; }
    public void setOrder(Order order)           { this.order = order; }


    public void setPaymentRef(String txnRef) {
        this.paymentRef = txnRef;
    }

    public void setPaidDate(LocalDateTime dateTime) {
        this.paidDate = dateTime;
    }

    @Transient
    public boolean isPaid() {
        return paidDate != null;
    }
}