package st.cbse.crm.bean;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.crm.data.*;
import st.cbse.crm.data.enums.OrderStatus;

import java.util.*;

@Stateless
class OrderBean {
    @PersistenceContext
    private EntityManager em;

    public UUID createOrder(UUID customerId, List<PrintingRequest> requests) {
        Customer customer = em.find(Customer.class, customerId);
        Order order = new Order();
        order.setCustomer(customer);
        order.setCreationDate(new Date());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setPaid(false);
        order.setPrintingRequests(requests);
        em.persist(order);
        return order.getId();
    }
}
