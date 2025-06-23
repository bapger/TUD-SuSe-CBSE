package st.cbse.crm.interfaces;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.Remote;
import st.cbse.crm.data.PrintingRequest;

@Remote
public interface IOrderMgmt {


	UUID createOrder(UUID customerId, String description);

}
