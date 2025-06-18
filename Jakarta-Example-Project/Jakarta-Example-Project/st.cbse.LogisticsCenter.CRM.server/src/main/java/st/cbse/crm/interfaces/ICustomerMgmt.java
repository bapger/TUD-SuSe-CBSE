package st.cbse.crm.interfaces;

import jakarta.ejb.Remote;
import java.util.UUID;

@Remote
public interface ICustomerMgmt {
    UUID registerCustomer(String name, String email, String password);
}
