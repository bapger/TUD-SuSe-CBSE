package st.cbse.crm.bean;

import jakarta.ejb.LocalBean;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.UUID;
import st.cbse.crm.data.*;
import st.cbse.crm.interfaces.ICustomerMgmt;

@Stateless
public class CustomerBean implements ICustomerMgmt {
    @PersistenceContext private EntityManager em;

    public UUID registerCustomer(String name, String email, String password) {
        Address addr = new Address("12345", "Default Street", "City");
        Customer c = new Customer(name, email, password, addr);
        em.persist(c);
        return c.getId();
    }

    public UUID loginCustomer(String email, String password) {
        TypedQuery<Customer> query = em.createQuery(
            "SELECT c FROM Customer c WHERE c.email = :email AND c.password = :pass", Customer.class);
        query.setParameter("email", email).setParameter("pass", password);
        return query.getSingleResult().getId();
    }

}

