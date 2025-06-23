package st.cbse.crm.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import st.cbse.crm.data.Manager;
import st.cbse.crm.interfaces.IManagerMgmt;

@Stateless
public class ManagerBean implements IManagerMgmt{
	@PersistenceContext private EntityManager em;
    @PostConstruct
    public void initManager() {
        if (em.find(Manager.class, "admin@crm.com") == null) {
            em.persist(new Manager("admin@crm.com", "admin123"));
        }
    }

    public boolean loginManager(String email, String password) {
        Manager m = em.find(Manager.class, email);
        return m != null && m.getPassword().equals(password);
    }
}
