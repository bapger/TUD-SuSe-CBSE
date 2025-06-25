package st.cbse.crm.managerComponent.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.crm.managerComponent.data.Manager;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;

@Stateless
public class ManagerBean implements IManagerMgmt{
    @PersistenceContext 
    private EntityManager em;
    
    @EJB
    private IOrderMgmt orderService;
    
    @EJB
    private IProcessMgmt processService;
    
    @PostConstruct
    public void initManager() {
        if (em.find(Manager.class, "admin") == null) {
            em.persist(new Manager("admin", "admin"));
        }
    }

    @Override
    public UUID loginManager(String email, String password) {
        try {
            TypedQuery<UUID> query = em.createQuery(
                "SELECT m.id " +
                "FROM   Manager m " +
                "WHERE  m.email    = :email " +
                "AND    m.password = :pwd",
                UUID.class);

            query.setParameter("email", email);
            query.setParameter("pwd",   password);    
            return query.getSingleResult(); 
        }
        catch (NoResultException ex) {
            throw new NoResultException();
        }
    }
    
    @Override
    public List<UUID> sendPrintToProd(UUID orderId) {
        try {
            OrderDTO orderDTO = orderService.getOrderDTO(orderId);
            
            List<UUID> processIds = new ArrayList<UUID>();
            
            for (PrintRequestDTO printRequestDTO : orderDTO.getPrintingRequests()) {
                UUID processId = processService.createProcessFromPrintRequest(printRequestDTO);
                processIds.add(processId);
                
                System.out.println("PrintingRequest " + printRequestDTO.getId() + 
                                 " sent to production. Process ID: " + processId);
            }
            
            System.out.println("Order " + orderId + 
                             " fully sent to production. Total processes created: " + processIds.size());
            
            return processIds;
            
        } catch (NoResultException ex) {
            throw new NoResultException("Order not found: " + orderId);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send print requests to production: " + ex.getMessage(), ex);
        }
    }
}