package st.cbse.crm.orderComponent.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.crm.customerComponent.data.Customer;
import st.cbse.crm.dto.OrderDTO;
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

        pr.add(option);
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

    /* ============================================================= */
    /* 5.  Order history (DTOs, not entities)                        */
    /* ============================================================= */
    @Override
    public List getOrdersByCustomer(UUID customerId) {

	EntityGraph g = em.createEntityGraph(Order.class);
	Subgraph pr = g.addSubgraph("printingRequests");
	pr.addSubgraph("options");

	List orders = em.createQuery(
			"SELECT DISTINCT o " +
					"FROM Order o " +
					"WHERE o.customer.id = :cid " +
					"ORDER BY o.creationDate DESC", Order.class)
			.setParameter("cid", customerId)
			.setHint("jakarta.persistence.fetchgraph", g) // <<< attach graph
			.getResultList();

    	return orders.stream()
    			.map(OrderDTO::of)
    			.toList();
    }

    /* ================================================================= */
    /*  Helpers                                                          */
    /* ================================================================= */
    private void recalcTotal(Order order) {
    	List<Option> ops;
        BigDecimal total = order.getBasePrice();
        for (PrintingRequest pr : order.getPrintingRequests()) {
        	ops = (List<Option>) pr.getOptions();
            for (Option op : ops)
                total = total.add(op.getPrice());
        }
        order.setTotal(total);
        
    }

    private BigDecimal unitPrice(Option option) {
        if (option instanceof PaintJob)   return new BigDecimal("4.00");
        if (option instanceof Smoothing)  return new BigDecimal("2.50");
        if (option instanceof Engraving)  return new BigDecimal("6.00");
        return BigDecimal.ZERO;
    }
}