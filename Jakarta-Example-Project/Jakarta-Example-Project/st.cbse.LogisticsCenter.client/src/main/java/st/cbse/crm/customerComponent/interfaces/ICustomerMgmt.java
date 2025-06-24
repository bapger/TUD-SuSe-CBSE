package st.cbse.crm.customerComponent.interfaces;

import jakarta.ejb.Remote;
import java.util.UUID;

@Remote
public interface ICustomerMgmt {
    UUID registerCustomer(String name, String email, String password);

	UUID loginCustomer(String email, String pw);
}
