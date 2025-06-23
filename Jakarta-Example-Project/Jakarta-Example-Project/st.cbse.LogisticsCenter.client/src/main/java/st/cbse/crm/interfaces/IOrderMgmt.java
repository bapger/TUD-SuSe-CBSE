package st.cbse.crm.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.data.PrintingRequest;

@Remote
public interface IOrderMgmt {


	UUID createOrder(UUID customerId, BigDecimal base);

	UUID addPrintRequest(UUID orderId, String stl, String note);

	void addPaintJobOption(UUID requestId, String colour, int layers);

	void addSmoothingOption(UUID requestId, String g);

	void addEngravingOption(UUID requestId, String text, String font, String img);

	void finalizeOrder(UUID orderId);

	void pay(UUID orderId, String ref);


}
