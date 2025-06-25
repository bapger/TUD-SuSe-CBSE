package st.cbse.crm.orderComponent.data;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Facture émise après expédition d’une commande.
 */
@Entity
public class Invoice {

    /* ================== clés & méta-données ================== */

    @Id
    private UUID id = UUID.randomUUID();

    /** Date d’émission de la facture. */
    private LocalDateTime issueDate = LocalDateTime.now();

    /** Montant total TTC de la commande. */
    private BigDecimal amount = BigDecimal.ZERO;

    /** Référence de paiement transmise par le client (peut rester null tant que non payée). */
    private String paymentRef;

    /** Date/heure à laquelle la facture a été payée (null = impayée). */
    private LocalDateTime paidDate;

    /* ================== relations ============================ */

    /** 
     * Lien 1-1 avec la commande. 
     * orphanRemoval = true pour supprimer la facture si la commande disparaît.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;

    /* ================== constructeurs ======================= */

    public Invoice() { }

    public Invoice(Order order, BigDecimal amount) {
        this.order  = order;
        this.amount = amount;
    }

    /* ================== getters / setters =================== */

    public UUID getId()                         { return id; }

    public LocalDateTime getIssueDate()         { return issueDate; }
    public void setIssueDate(LocalDateTime d)   { this.issueDate = d; }

    public BigDecimal getAmount()               { return amount; }
    public void setAmount(BigDecimal amount)    { this.amount = amount; }

    public String getPaymentRef()               { return paymentRef; }
    public LocalDateTime getPaidDate()          { return paidDate; }

    public Order getOrder()                     { return order; }
    public void setOrder(Order order)           { this.order = order; }

    /* ================== logique métier ====================== */

    /** Appelé lors de l’enregistrement du paiement. */
    public void setPaymentRef(String txnRef) {
        this.paymentRef = txnRef;
    }

    public void setPaidDate(LocalDateTime dateTime) {
        this.paidDate = dateTime;
    }

    /** Indique si la facture est réglée. */
    @Transient
    public boolean isPaid() {
        return paidDate != null;
    }
}