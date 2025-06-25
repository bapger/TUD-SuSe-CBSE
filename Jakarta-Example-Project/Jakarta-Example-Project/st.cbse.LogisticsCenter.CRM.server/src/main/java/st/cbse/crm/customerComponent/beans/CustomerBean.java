package st.cbse.crm.customerComponent.beans;

import jakarta.ejb.LocalBean;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.UUID;
import st.cbse.crm.customerComponent.data.*;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;

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
    	try {
            TypedQuery<Customer> query = em.createQuery(
                    "SELECT c FROM Customer c WHERE c.email = :email AND c.password = :pass", Customer.class);
            query.setParameter("email", email).setParameter("pass", password);
            return query.getSingleResult().getId();
    	} catch (NoResultException ex) {
            // bad credentials → sentinel or exception
    		throw new NoResultException();    // or throw AuthenticationException
        }
    }

}

