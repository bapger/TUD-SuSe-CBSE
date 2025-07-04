package st.cbse.crm.customerComponent.interfaces;

import jakarta.ejb.Remote;
import java.util.UUID;

@Remote
public interface ICustomerMgmt {
    UUID registerCustomer(String name, String email, String password) throws Exception;
    UUID loginCustomer(String email, String password);
}
