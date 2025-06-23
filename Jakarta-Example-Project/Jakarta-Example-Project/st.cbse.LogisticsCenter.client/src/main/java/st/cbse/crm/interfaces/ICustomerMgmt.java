package st.cbse.crm.interfaces;

import java.util.UUID;

import jakarta.ejb.Remote;

@Remote
public interface ICustomerMgmt {

	UUID registerCustomer(String string, String string2, String string3);

	UUID loginCustomer(String email2, String password2);

}
