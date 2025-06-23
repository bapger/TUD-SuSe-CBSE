package st.cbse.crm.orderComponent.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.crm.customerComponent.data.Customer;
import st.cbse.crm.orderComponent.data.*;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Stateless
public class OrderBean implements IOrderMgmt {

    @PersistenceContext
    private EntityManager em;

    @Override
    public UUID createOrder(UUID customerId, BigDecimal basePrice) {
        Customer customer = em.find(Customer.class, customerId);
        if (customer == null)
            throw new IllegalArgumentException("Customer not found: " + customerId);

        Order order = new Order();
        order.setCustomer(customer);
        order.setBasePrice(basePrice);
        order.setTotal(basePrice);
        order.setCreationDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.CREATED);
        em.persist(order);

        return order.getId();
    }

    /* ================================================================= */
    /* 2. Printing-request                                               */
    /* ================================================================= */
    @Override
    public UUID addPrintRequest(UUID orderId, String stlPath, String note) {
        Order order = em.find(Order.class, orderId);
        if (order == null)
            throw new IllegalArgumentException("Order not found: " + orderId);

        PrintingRequest pr = new PrintingRequest();
        pr.setOrder(order);
        pr.setStlPath(stlPath);
        pr.setNote(note);
        em.persist(pr);

        order.getPrintingRequests().add(pr);
        return pr.getId();
    }

    /* ================================================================= */
    /* 3.  Options                                                       */
    /* ================================================================= */
    @Override
    public void addPaintJobOption(UUID requestId, String colour, int layers) {
        Option opt = new PaintJob(colour,layers);
        addOption(requestId, opt);
    }

    @Override
    public void addSmoothingOption(UUID requestId, String granularity) {
        Option opt = new Smoothing(granularity);
        addOption(requestId, opt);
    }

    @Override
    public void addEngravingOption(UUID requestId,
                                   String text, String font, String imagePath) {
        Option opt = new Engraving(text,font,imagePath);
        addOption(requestId, opt);
    }

    /* ----------------------------------------------------------------- */
    private void addOption(UUID requestId, Option option) {
        PrintingRequest pr = em.find(PrintingRequest.class, requestId);
        if (pr == null)
            throw new IllegalArgumentException("Request not found: " + requestId);

        option.setPrice(unitPrice(option));     // prix unitaire
        option.setPrintingRequest(pr);
        em.persist(option);

        pr.getOptions().add(option);
        recalcTotal(pr.getOrder());
    }

    /* ================================================================= */
    /* 4.  Finalisation & paiement                                       */
    /* ================================================================= */
    @Override
    public void finalizeOrder(UUID orderId) {
        Order o = em.find(Order.class, orderId);
        if (o == null) throw new IllegalArgumentException("Order not found");
        if (o.getOrderStatus() != OrderStatus.CREATED)
            throw new IllegalStateException("Order already finalised");

        o.setOrderStatus(OrderStatus.COMPLETED);
    }

    @Override
    public void pay(UUID orderId, String txnRef) {
        Order o = em.find(Order.class, orderId);
        if (o == null) throw new IllegalArgumentException("Order not found");
        if (o.getOrderStatus() != OrderStatus.SHIPPED)
            throw new IllegalStateException("Order must be shipped first");

        Invoice inv = o.getInvoice();
        if (inv == null) throw new IllegalStateException("No invoice for order");

        inv.setPaymentRef(txnRef);
        inv.setPaidDate(LocalDateTime.now());
        o.setOrderStatus(OrderStatus.COMPLETED);
    }

    /* ================================================================= */
    /* 5.  Historique (entités JPA renvoyées côté serveur uniquement)    */
    /* ================================================================= */
    @Override
    public List<String> getOrderHistoryLines(UUID customerId) {

        // On charge tout ce qu’il faut en une seule requête.
        List<Order> orders = em.createQuery(
            "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.printingRequests pr " +
            "LEFT JOIN FETCH pr.options " +
            "WHERE o.customer.id = :cid ORDER BY o.creationDate DESC",
            Order.class)
            .setParameter("cid", customerId)
            .getResultList();

        List<String> lines = new ArrayList<>();

        for (Order o : orders) {
            lines.add(String.format(
                "Order %s   Status: %s   Total: €%s",
                o.getId(), o.getOrderStatus(), o.getTotal()));

            for (PrintingRequest pr : o.getPrintingRequests()) {
                lines.add("  Request " + pr.getId() + "   STL=" + pr.getStlPath());

                if (pr.getNote() != null && !pr.getNote().isBlank())
                    lines.add("    Note: " + pr.getNote());

                for (Option op : pr.getOptions()) {
                    lines.add(String.format(
                        "    %-15s  €%s",
                        op.getClass().getSimpleName(), op.getPrice()));
                }
            }
        }
        return lines;         // ← rien d’autre que des String
    }

    /* ================================================================= */
    /*  Helpers                                                          */
    /* ================================================================= */
    private void recalcTotal(Order order) {
        BigDecimal total = order.getBasePrice();
        for (PrintingRequest pr : order.getPrintingRequests())
            for (Option op : pr.getOptions())
                total = total.add(op.getPrice());
        order.setTotal(total);
    }

    private BigDecimal unitPrice(Option option) {
        if (option instanceof PaintJob)   return new BigDecimal("4.00");
        if (option instanceof Smoothing)  return new BigDecimal("2.50");
        if (option instanceof Engraving)  return new BigDecimal("6.00");
        return BigDecimal.ZERO;
    }
}