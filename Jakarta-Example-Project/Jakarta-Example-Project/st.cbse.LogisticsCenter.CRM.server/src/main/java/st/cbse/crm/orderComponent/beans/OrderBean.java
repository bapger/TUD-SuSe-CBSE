package st.cbse.crm.orderComponent.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.crm.orderComponent.data.*;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.productionFacility.production.beans.ProductionBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;


@Stateless
public class OrderBean implements IOrderMgmt {

	@PersistenceContext
	private EntityManager em;

	private static final Logger LOG = Logger.getLogger(ProductionBean.class.getName());
	
	@Override
	public UUID createOrder(UUID customerId, BigDecimal basePrice) {
		if (customerId == null)
			throw new IllegalArgumentException("Customer ID cannot be null");

		Long nb = em.createQuery(
				"SELECT COUNT(c) FROM Customer c WHERE c.id = :cid",
				Long.class)
				.setParameter("cid", customerId)
				.getSingleResult();

		if (nb == 0) {
			throw new IllegalArgumentException(
					"Customer not found: " + customerId);
		}

		Order order = new Order();
		order.setCustomerId(customerId);
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
		pr.setOrderId(orderId);
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

	private void addOption(UUID requestId, Option option) {
		PrintingRequest pr = em.find(PrintingRequest.class, requestId);
		if (pr == null)
			throw new IllegalArgumentException("Request not found: " + requestId);

		option.setPrice(unitPrice(option));  
		option.setPrintingRequest(pr);
		em.persist(option);

		pr.add(option);
		recalcTotal(pr.getOrder());
	}

	/* ================================================================= */
	/* 4.  Finalization & Paying                                       */
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
	/* 5.  Order history                 */
	/* ============================================================= */
	@Override
	public List<OrderDTO> getOrdersByCustomer(UUID customerId) {
	    List<Order> orders = em.createQuery(
	            "SELECT DISTINCT o " +
	            "FROM Order o " +
	            "LEFT JOIN FETCH o.printingRequests " +
	            "WHERE o.customerId = :cid " +
	            "ORDER BY o.creationDate DESC", Order.class)
	            .setParameter("cid", customerId)
	            .getResultList();

	    for (Order order : orders) {
	        for (PrintingRequest pr : order.getPrintingRequests()) {
	            pr.getOptions().size(); 
	        }
	    }

	    return orders.stream()
	            .map(OrderDTO::of)
	            .collect(java.util.stream.Collectors.toList());
	}
	
	public UUID getOrderIdByPrintRequestId(UUID printRequestId) {
	    return em.createQuery(
	        "SELECT pr.orderId FROM PrintingRequest pr WHERE pr.id = :printRequestId", 
	        UUID.class)
	        .setParameter("printRequestId", printRequestId)
	        .getSingleResult();
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

	@Override
	public PrintRequestDTO getPrintRequestDTO(UUID printingRequestId) {
		if (printingRequestId == null) {
			throw new IllegalArgumentException("PrintingRequest ID cannot be null");
		}

		PrintingRequest printRequest = em.createQuery(
				"SELECT pr FROM PrintingRequest pr " +
						"LEFT JOIN FETCH pr.options " +
						"WHERE pr.id = :id", PrintingRequest.class)
				.setParameter("id", printingRequestId)
				.getSingleResult();

		if (printRequest == null) {
			throw new NoResultException("PrintingRequest not found with ID: " + printingRequestId);
		}

		return PrintRequestDTO.of(printRequest);
	}

	@Override
	public OrderDTO getOrderDTO(UUID orderId) {
		if (orderId == null) {
			throw new IllegalArgumentException("Order ID cannot be null");
		}

		Order order = em.createQuery(
				"SELECT DISTINCT o " +
						"FROM   Order o " +
						"LEFT   JOIN FETCH o.printingRequests " +
						"WHERE  o.id = :id",
						Order.class)
				.setParameter("id", orderId)
				.getSingleResult();

		return OrderDTO.of(order);
	}

	@Override
	public List<OrderDTO> fetchAllOrderDTOs() {
	    List<Order> orders = em.createQuery(
	            "SELECT DISTINCT o " +
	            "FROM Order o " +
	            "LEFT JOIN FETCH o.printingRequests " +
	            "ORDER BY o.creationDate DESC", Order.class)
	            .getResultList();

	    // Forcer le chargement des options
	    for (Order order : orders) {
	        for (PrintingRequest pr : order.getPrintingRequests()) {
	            pr.getOptions().size(); 
	        }
	    }

	    return orders.stream()
	            .map(OrderDTO::of)
	            .collect(java.util.stream.Collectors.toList());
	}

	@Override
	public void addNoteToPrintRequest(UUID requestId, String note) {

		if (requestId == null) {
			throw new IllegalArgumentException("requestId must not be null");
		}
		if (note == null || note.isBlank()) {
			throw new IllegalArgumentException("note must not be empty");
		}

		try {
			PrintingRequest pr = em.createQuery(
					"SELECT pr FROM PrintingRequest pr WHERE pr.id = :id",
					PrintingRequest.class)
					.setParameter("id", requestId)
					.getSingleResult();
			pr.setNote(note);

			em.flush();
		}
		catch (NoResultException ex) {
			throw new NoResultException("PrintRequest not found: " + requestId);
		}
	}

	@Override
	public void updateStatus(UUID orderId, String status) {
		Order order = em.find(Order.class, orderId);
		if (order == null)
			throw new IllegalArgumentException("Order not found: " + orderId);
		order.setOrderStatus(OrderStatus.valueOf(status.toUpperCase(Locale.ROOT)));	
	}

	@Override
	public void updateStatusPrintingRequest(UUID printingRequestIDs, String status) {
	    LOG.info("========================================");
	    LOG.info("Updating PrintingRequest status");
	    LOG.info("PrintingRequest ID: " + printingRequestIDs);
	    LOG.info("New status: " + status);
	    
	    PrintingRequest pr = em.find(PrintingRequest.class, printingRequestIDs);
	    if (pr == null) {
	        LOG.severe("Printing request not found: " + printingRequestIDs);
	        throw new IllegalArgumentException("Printing request not found: " + printingRequestIDs);
	    }
	    
	    LOG.info("Found PrintingRequest - Current status: " + pr.getStatus());
	    LOG.info("Order ID: " + pr.getOrderId());
	    
	    PrintingRequestStatus newStatus = PrintingRequestStatus.valueOf(status.toUpperCase(Locale.ROOT));
	    pr.setStatus(newStatus);
	    em.merge(pr);
	    
	    LOG.info("PrintingRequest status updated to: " + newStatus);
	    
	    if ("IN_STORAGE".equals(status)) {
	        LOG.info("Checking if all PrintingRequests of order " + pr.getOrderId() + " are complete");
	        
	        Order order = em.find(Order.class, pr.getOrderId());
	        if (order == null) {
	            LOG.warning("Order not found for ID: " + pr.getOrderId());
	            return;
	        }
	        
	        LOG.info("Order status before check: " + order.getOrderStatus());
	        
	        List<PrintingRequest> allRequests = order.getPrintingRequests();
	        LOG.info("Order has " + allRequests.size() + " PrintingRequests");
	        
	        boolean allInStorage = true;
	        for (PrintingRequest request : allRequests) {
	            LOG.info("  PrintingRequest " + request.getId() + ": " + request.getStatus());
	            if (request.getStatus() != PrintingRequestStatus.IN_STORAGE) {
	                allInStorage = false;
	            }
	        }
	        
	        if (allInStorage) {
	            LOG.info("✓ All PrintingRequests are IN_STORAGE - Marking order as FINISHED");
	            order.setOrderStatus(OrderStatus.FINISHED);
	            em.merge(order);
	        } else {
	            LOG.info("✗ Not all PrintingRequests are IN_STORAGE yet - Order remains: " + order.getOrderStatus());
	        }
	    }
	    
	    LOG.info("========================================");
	}
	
	
	// In OrderBean class, add these methods:

	@Override
	public void createInvoiceForShippedOrder(UUID orderId) throws Exception {
	    // Find the order
	    Order order = em.find(Order.class, orderId);
	    if (order == null) {
	        throw new IllegalArgumentException("Order not found: " + orderId);
	    }
	    
	    // Verify order is in correct status
	    if (order.getOrderStatus() != OrderStatus.FINISHED) {
	        throw new IllegalStateException("Can only create invoice for FINISHED orders. Current status: " + order.getOrderStatus());
	    }
	    
	    // Check if invoice already exists
	    List<Invoice> existingInvoices = em.createQuery(
	        "SELECT i FROM Invoice i WHERE i.order.id = :orderId", Invoice.class)
	        .setParameter("orderId", orderId)
	        .getResultList();
	    
	    if (!existingInvoices.isEmpty()) {
	        throw new IllegalStateException("Invoice already exists for order: " + orderId);
	    }
	    
	    // Create the invoice
	    Invoice invoice = new Invoice(order, order.getTotal());
	    em.persist(invoice);
	    
	    // Update order status to SHIPPED
	    order.setOrderStatus(OrderStatus.SHIPPED);
	    em.merge(order);
	    
	    LOG.info("Created invoice " + invoice.getId() + " for order " + orderId);
	}

	@Override
	public void payInvoice(UUID orderId, String paymentReference) throws Exception {
	    // Find the order
	    Order order = em.find(Order.class, orderId);
	    if (order == null) {
	        throw new IllegalArgumentException("Order not found: " + orderId);
	    }
	    
	    // Find the invoice for this order
	    Invoice invoice;
	    try {
	        invoice = em.createQuery(
	            "SELECT i FROM Invoice i WHERE i.order.id = :orderId", Invoice.class)
	            .setParameter("orderId", orderId)
	            .getSingleResult();
	    } catch (NoResultException e) {
	        throw new IllegalStateException("No invoice found for order: " + orderId);
	    }
	    
	    // Check if already paid
	    if (invoice.isPaid()) {
	        throw new IllegalStateException("Invoice already paid on: " + invoice.getPaidDate());
	    }
	    
	    // Record the payment
	    invoice.setPaymentRef(paymentReference);
	    invoice.setPaidDate(LocalDateTime.now());
	    em.merge(invoice);
	    
	    LOG.info("Invoice " + invoice.getId() + " paid with reference: " + paymentReference);
	}

	public boolean hasUnpaidInvoice(UUID orderId) throws Exception {
	    // Find the order
	    Order order = em.find(Order.class, orderId);
	    if (order == null) {
	        throw new IllegalArgumentException("Order not found: " + orderId);
	    }
	    
	    // Check if order is shipped (only shipped orders have invoices)
	    if (order.getOrderStatus() != OrderStatus.SHIPPED) {
	        return false;
	    }
	    
	    // Find invoice for this order
	    List<Invoice> invoices = em.createQuery(
	        "SELECT i FROM Invoice i WHERE i.order.id = :orderId", Invoice.class)
	        .setParameter("orderId", orderId)
	        .getResultList();
	    
	    if (invoices.isEmpty()) {
	        return false;  // No invoice exists
	    }
	    
	    // Check if the invoice is unpaid
	    Invoice invoice = invoices.get(0);  // Should only be one invoice per order
	    return !invoice.isPaid();
	}
	
	

}