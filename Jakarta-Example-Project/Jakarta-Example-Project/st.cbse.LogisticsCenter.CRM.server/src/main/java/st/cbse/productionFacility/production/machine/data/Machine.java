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
    
    @Column(name = "input_id")
    private UUID inputId;
    
    @Column(name = "output_id")
    private UUID outputId;
    
    @Column(name = "current_product_id")
    private UUID currentProductId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    // Constructeur
    public Machine() {
        this.status = MachineStatus.AVAILABLE;
    }
    
    // Méthodes métier
    public void activate() {
        this.status = MachineStatus.ACTIVE;
    }
    
    public void reserve() {
        this.status = MachineStatus.RESERVED;
    }
    
    public void release() {
        this.status = MachineStatus.AVAILABLE;
        this.currentProductId = null;
    }
    
    public boolean isAvailable() {
        return this.status == MachineStatus.AVAILABLE;
    }
    
    // Getters et Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public MachineStatus getStatus() {
        return status;
    }
    
    public void setStatus(MachineStatus status) {
        this.status = status;
    }
    
    public UUID getInputId() {
        return inputId;
    }
    
    public void setInputId(UUID inputId) {
        this.inputId = inputId;
    }
    
    public UUID getOutputId() {
        return outputId;
    }
    
    public void setOutputId(UUID outputId) {
        this.outputId = outputId;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}