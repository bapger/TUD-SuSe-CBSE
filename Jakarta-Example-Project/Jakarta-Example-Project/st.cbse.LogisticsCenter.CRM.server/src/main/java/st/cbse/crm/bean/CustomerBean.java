package st.cbse.crm.bean;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.UUID;
import st.cbse.crm.data.*;
import st.cbse.crm.interfaces.ICustomerMgmt;

@Stateless
@LocalBean
public class CustomerBean implements ICustomerMgmt {
    @PersistenceContext
    private EntityManager em;

    public UUID registerCustomer(String name, String email, String password) {
        Address address = new Address("12345", "Main Street", "Berlin");
        Customer customer = new Customer(name, email, password, address);
        em.persist(customer);
        return customer.getId();
    }
}
