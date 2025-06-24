package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "machines")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "machine_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Machine {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MachineStatus status = MachineStatus.AVAILABLE;

    @Column(name = "current_product_id")
    private UUID currentProductId;

    @Column(name = "name")
    private String name;

    protected abstract long shutdownDelayMillis();
    protected abstract String buildMessage(String action);

    public void activate() {
        status = MachineStatus.ACTIVE;
        System.out.println(buildMessage("activée"));
    }

    public void reserve() {
        status = MachineStatus.RESERVED;
        System.out.println(buildMessage("réservée"));
    }

    public void release() {
        status = MachineStatus.AVAILABLE;
        currentProductId = null;
        System.out.println(buildMessage("libérée"));
    }

    public boolean available() {
        return status == MachineStatus.AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public UUID getCurrentProductId() {
        return currentProductId;
    }

    public void setCurrentProductId(UUID currentProductId) {
        this.currentProductId = currentProductId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getShutdownDelayMillis() {
        return shutdownDelayMillis();
    }

    public String message(String action) {
        return buildMessage(action);
    }
}