package st.cbse.productionFacility.productionManagerComponent.beans;

import st.cbse.productionFacility.productionManagerComponent.interfaces.IProductionManagerMgmt;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import st.cbse.productionFacility.process.dto.ProcessDTO;
import st.cbse.productionFacility.productionManagerComponent.data.ProductionManager;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;

@Stateless
public class ProductionManagerBean implements IProductionManagerMgmt {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private IProcessMgmt processService;

    @PostConstruct
    public void initProductionManager() {
        if (em.find(ProductionManager.class, "prodManager") == null) {
            em.persist(new ProductionManager("prodManager", "prodManager"));
        }
    }

    @Override
    public String loginProductionManager(String email, String password) {
        try {
            return em.createQuery(
                    "SELECT pm.email FROM ProductionManager pm " +
                            "WHERE pm.email = :mail AND pm.password = :pwd",
                    String.class)
                    .setParameter("mail", email)
                    .setParameter("pwd", password)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new NoResultException();
        }
    }

    public List<ProcessDTO> getAllProcesses() {
        return processService.getAllProcesses();
    }
}
