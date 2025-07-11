package st.cbse.crm.orderComponent.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.dto.OrderDTO;

@Remote
public interface IOrderMgmt {

	UUID createOrder(UUID customerId, BigDecimal basePrice);

	UUID addPrintRequest(UUID orderId, String stlPath, String note);

	void addPaintJobOption(UUID requestId, String colour, int layers);

	void addSmoothingOption(UUID requestId, String granularity);

	void addEngravingOption(UUID requestId, String text, String font, String imagePath);

	void finalizeOrder(UUID orderId);

	void pay(UUID orderId, String txnRef);

	List<OrderDTO> getOrdersByCustomer(UUID customerId);

	void createInvoiceForShippedOrder(UUID id);

	boolean hasUnpaidInvoice(UUID id);

	void payInvoice(UUID id, String ref);

}
