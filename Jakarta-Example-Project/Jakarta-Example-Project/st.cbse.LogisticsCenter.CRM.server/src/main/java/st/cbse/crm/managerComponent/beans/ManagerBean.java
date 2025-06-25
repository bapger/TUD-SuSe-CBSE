package st.cbse.crm.managerComponent.beans;

import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import st.cbse.crm.managerComponent.data.Manager;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;

@Stateless
public class ManagerBean implements IManagerMgmt{
	@PersistenceContext private EntityManager em;
    @PostConstruct
    public void initManager() {
        if (em.find(Manager.class, "admin") == null) {
            em.persist(new Manager("admin", "admin"));
        }
    }

    @Override
    public UUID loginManager(String email, String password) {

        try {
            // 1) requête JPQL typée : on précise le type de retour ==> plus de cast !
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
            // bad credentials → sentinel or exception
        	throw new NoResultException();    // or throw AuthenticationException
        }
    }
}
