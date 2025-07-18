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
import jakarta.ws.rs.NotAllowedException;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.crm.managerComponent.data.Manager;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;

@Stateless
public class ManagerBean implements IManagerMgmt {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private IOrderMgmt orderService;

    @EJB
    private IProcessMgmt processService;

    @PostConstruct
    public void initManager() {
        if (em.find(Manager.class, "admin@gmail.com") == null) {
            em.persist(new Manager("admin@gmail.com", "admin"));
        }
    }

    @Override
    public String loginManager(String email, String password) {
        try {
            return em.createQuery(
                    "SELECT m.email FROM Manager m " +
                            "WHERE m.email = :mail AND m.password = :pwd",
                    String.class)
                    .setParameter("mail", email)
                    .setParameter("pwd", password)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new NoResultException();
        }
    }

    @Override
    public List<UUID> sendPrintToProd(UUID orderId) {
        try {
            OrderDTO orderDTO = orderService.getOrderDTO(orderId);
            if(orderDTO.getStatus().toString()=="IN_PROD") {
            	throw new NotAllowedException("Order already in production");
            }
            List<UUID> processIds = new ArrayList<UUID>();

            for (PrintRequestDTO printRequestDTO : orderDTO.getPrintingRequests()) {
                UUID processId = processService.createProcessFromPrintRequest(printRequestDTO);
                processIds.add(processId);

                System.out.println("PrintingRequest " + printRequestDTO.getId() +
                        " sent to production. Process ID: " + processId);
            }

            System.out.println("Order " + orderId +
                    " fully sent to production. Total processes created: " + processIds.size());
            orderService.updateStatus(orderId, "IN_PROD");
            return processIds;

        } catch (NoResultException ex) {
            throw new NoResultException("Order not found: " + orderId);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send print requests to production: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<OrderDTO> listAllOrders() {
        return orderService.fetchAllOrderDTOs();
    }

    @Override
    public void addNoteToRequest(UUID requestId, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Note must not be empty");
        }
        try {
            orderService.addNoteToPrintRequest(requestId, note);
        } catch (NoResultException ex) {
            throw new NoResultException("PrintRequest not found: " + requestId);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to add note to request " + requestId + " : " + ex.getMessage(), ex);
        }
    }

}