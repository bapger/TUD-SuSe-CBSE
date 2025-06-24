package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PRINTING")
public class PrintingMachine extends Machine {
    
    @Column(name = "max_print_volume")
    private Double maxPrintVolume; // en cm³
    
    @Column(name = "resolution")
    private Double resolution; // en mm
    
    @Column(name = "supported_materials")
    private String supportedMaterials; // PLA, ABS, PETG, etc.
    
    public PrintingMachine() {
        super();
        setName("3D Printing Machine");
        setDescription("Creates 3D objects from digital files");
        // Pas d'input, seulement output
        setInputId(null);
    }
    
    // Getters et Setters
    public Double getMaxPrintVolume() {
        return maxPrintVolume;
    }
    
    public void setMaxPrintVolume(Double maxPrintVolume) {
        this.maxPrintVolume = maxPrintVolume;
    }
    
    public Double getResolution() {
        return resolution;
    }
    
    public void setResolution(Double resolution) {
        this.resolution = resolution;
    }
    
    public String getSupportedMaterials() {
        return supportedMaterials;
    }
    
    public void setSupportedMaterials(String supportedMaterials) {
        this.supportedMaterials = supportedMaterials;
    }
}