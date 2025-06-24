package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SMOOTHING")
public class SmoothingMachine extends Machine {
    
    @Column(name = "smoothing_method")
    private String smoothingMethod; // CHEMICAL_VAPOR, SANDING, HEAT
    
    @Column(name = "processing_time")
    private Integer processingTime; // en secondes
    
    @Column(name = "temperature")
    private Double temperature; // Pour les méthodes thermiques
    
    public SmoothingMachine() {
        super();
        setName("Surface Smoothing Machine");
        setDescription("Smooths the surface of 3D printed objects");
    }
    
    // Getters et Setters
    public String getSmoothingMethod() {
        return smoothingMethod;
    }
    
    public void setSmoothingMethod(String smoothingMethod) {
        this.smoothingMethod = smoothingMethod;
    }
    
    public Integer getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(Integer processingTime) {
        this.processingTime = processingTime;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}