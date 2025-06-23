package st.cbse.crm.bean;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.crm.data.*;
import st.cbse.crm.data.enums.OrderStatus;
import st.cbse.crm.interfaces.IOrderMgmt;

import java.util.*;

@Stateless
public class OrderBean implements IOrderMgmt{
    @PersistenceContext
    private EntityManager em;

    public UUID createOrder(UUID customerId, String description) {
        Customer customer = em.find(Customer.class, customerId);
        if (customer == null) throw new IllegalArgumentException("Customer not found: " + customerId);

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreationDate(new Date());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setPaid(false);

        PrintingRequest request = new PrintingRequest();
        request.setDescription(description);
        request.setOrder(order);

        order.setPrintingRequests(List.of(request));

        em.persist(order);
        return order.getId();
    }
    
    public UUID createPrintingRequest(String description, UUID orderId, Option option) {
        // Récupère la commande
        Order order = em.find(Order.class, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found for ID: " + orderId);
        }

        // Crée une nouvelle printing request
        PrintingRequest request = new PrintingRequest();
        request.setDescription(description);
        request.setOrder(order);
        request.addOption(option);

        // Ajoute à la liste des demandes d'impression
        if (order.getPrintingRequests() == null) {
            order.setPrintingRequests(new ArrayList<>());
        }
        order.getPrintingRequests().add(request);

        // Persiste uniquement la printing request
        em.persist(request);

        return request.getId(); // ← cohérent avec createOrder
    }


}
