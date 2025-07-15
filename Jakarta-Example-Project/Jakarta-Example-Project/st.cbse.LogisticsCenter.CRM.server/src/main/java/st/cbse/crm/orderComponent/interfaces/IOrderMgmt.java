package st.cbse.crm.orderComponent.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;

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
	
	PrintRequestDTO getPrintRequestDTO(UUID printingRequestId);
	OrderDTO getOrderDTO(UUID orderId);

	void addNoteToPrintRequest(UUID requestId, String note);

	void updateStatus(UUID orderId, String status);
	
	void updateStatusPrintingRequest(UUID printingRequestIDs, String status);

	List<OrderDTO> fetchAllOrderDTOs();

	void createInvoiceForShippedOrder(UUID orderId) throws Exception;

	void payInvoice(UUID orderId, String paymentReference) throws Exception;

	boolean hasUnpaidInvoice(UUID orderId) throws Exception;

	UUID getOrderIdByPrintRequestId(UUID printRequestId);

}
