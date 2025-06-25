package st.cbse.productionFacility.production.machine.data;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Logger;

@Singleton
@Startup
public class MachineInitializerBean {
    
    private static final Logger LOG = Logger.getLogger(MachineInitializerBean.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
    @PostConstruct
    public void initializeMachines() {
        LOG.info("========================================");
        LOG.info("Initializing production machines...");
        
        // Vérifier si des machines existent déjà
        Long machineCount = em.createQuery("SELECT COUNT(m) FROM Machine m", Long.class)
                              .getSingleResult();
        
        if (machineCount > 0) {
            LOG.info("Machines already exist in database (" + machineCount + " machines). Skipping initialization.");
            return;
        }
        
        // Créer les différents types de machines
        createPrintingMachines();
        createPaintMachines();
        createSmoothingMachines();
        createEngravingMachines();
        createPackagingMachines();
        
        // Compter le total des machines créées
        Long totalMachines = em.createQuery("SELECT COUNT(m) FROM Machine m", Long.class)
                               .getSingleResult();
        
        LOG.info("Machine initialization completed! Total machines created: " + totalMachines);
        LOG.info("========================================");
    }
    
    private void createPrintingMachines() {
        LOG.info("Creating Printing Machines...");
        
        // Créer 3 machines d'impression 3D
        for (int i = 1; i <= 3; i++) {
            PrintingMachine machine = new PrintingMachine(true, true);
            machine.setStatus(MachineStatus.AVAILABLE);
            em.persist(machine);
            LOG.info("  ✓ Created PrintingMachine #" + i + " (ID: " + machine.getId() + ")");
        }
    }
    
    private void createPaintMachines() {
        LOG.info("Creating Paint Machines...");
        
        // Créer 2 machines de peinture
        for (int i = 1; i <= 2; i++) {
            PaintMachine machine = new PaintMachine(true, true);
            machine.setStatus(MachineStatus.AVAILABLE);
            em.persist(machine);
            LOG.info("  ✓ Created PaintMachine #" + i + " (ID: " + machine.getId() + ")");
        }
    }
    
    private void createSmoothingMachines() {
        LOG.info("Creating Smoothing Machines...");
        
        // Créer 2 machines de lissage
        for (int i = 1; i <= 2; i++) {
            SmoothingMachine machine = new SmoothingMachine(true, true);
            machine.setStatus(MachineStatus.AVAILABLE);
            em.persist(machine);
            LOG.info("  ✓ Created SmoothingMachine #" + i + " (ID: " + machine.getId() + ")");
        }
    }
    
    private void createEngravingMachines() {
        LOG.info("Creating Engraving Machines...");
        
        // Créer 1 machine de gravure (plus spécialisée, donc moins nombreuse)
        EngravingMachine machine = new EngravingMachine(true, true);
        machine.setStatus(MachineStatus.AVAILABLE);
        em.persist(machine);
        LOG.info("  ✓ Created EngravingMachine #1 (ID: " + machine.getId() + ")");
    }
    
    private void createPackagingMachines() {
        LOG.info("Creating Packaging Machines...");
        
        // Créer 2 machines d'emballage
        for (int i = 1; i <= 2; i++) {
            PackagingMachine machine = new PackagingMachine(true, true);
            machine.setStatus(MachineStatus.AVAILABLE);
            em.persist(machine);
            LOG.info("  ✓ Created PackagingMachine #" + i + " (ID: " + machine.getId() + ")");
        }
    }
    
    // Méthode utilitaire pour réinitialiser toutes les machines (utile pour les tests)
    public void resetAllMachines() {
        LOG.info("Resetting all machines to AVAILABLE status...");
        
        int updated = em.createQuery(
            "UPDATE Machine m SET " +
            "m.status = :status, " +
            "m.activeProcessId = NULL, " +
            "m.inputProcessId = NULL, " +
            "m.outputProcessId = NULL")
            .setParameter("status", MachineStatus.AVAILABLE)
            .executeUpdate();
        
        LOG.info("Reset " + updated + " machines to AVAILABLE status");
    }
    
    // Méthode pour afficher l'état actuel des machines
    public void displayMachineStatus() {
        LOG.info("=== Current Machine Status ===");
        
        em.createQuery("SELECT m FROM Machine m ORDER BY TYPE(m), m.id", Machine.class)
          .getResultList()
          .forEach(machine -> {
              LOG.info(String.format("Machine [%s] - Type: %s, Status: %s, Active: %s",
                  machine.getId(),
                  machine.getMachineType(),
                  machine.getStatus(),
                  machine.getActiveProcessId() != null ? machine.getActiveProcessId() : "None"
              ));
          });
    }
}