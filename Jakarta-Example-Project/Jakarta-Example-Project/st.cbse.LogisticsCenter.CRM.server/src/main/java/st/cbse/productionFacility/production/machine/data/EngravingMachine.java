package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ENGRAVING")
public class EngravingMachine extends Machine {
    
    @Column(name = "engraving_method")
    private String engravingMethod; // LASER, MECHANICAL, CHEMICAL
    
    @Column(name = "max_engraving_area")
    private Double maxEngravingArea; // en cm²
    
    @Column(name = "precision")
    private Double precision; // en mm
    
    @Column(name = "supports_images")
    private Boolean supportsImages = true;
    
    public EngravingMachine() {
        super();
        setName("Engraving Machine");
        setDescription("Engraves text and images on object surfaces");
    }
    
    // Getters et Setters
    public String getEngravingMethod() {
        return engravingMethod;
    }
    
    public void setEngravingMethod(String engravingMethod) {
        this.engravingMethod = engravingMethod;
    }
    
    public Double getMaxEngravingArea() {
        return maxEngravingArea;
    }
    
    public void setMaxEngravingArea(Double maxEngravingArea) {
        this.maxEngravingArea = maxEngravingArea;
    }
    
    public Double getPrecision() {
        return precision;
    }
    
    public void setPrecision(Double precision) {
        this.precision = precision;
    }
    
    public Boolean getSupportsImages() {
        return supportsImages;
    }
    
    public void setSupportsImages(Boolean supportsImages) {
        this.supportsImages = supportsImages;
    }
}