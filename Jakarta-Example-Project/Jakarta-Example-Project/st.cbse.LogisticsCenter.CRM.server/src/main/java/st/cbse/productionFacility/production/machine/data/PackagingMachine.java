package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PACKAGING")
public class PackagingMachine extends Machine {
    
    @Column(name = "packaging_type")
    private String packagingType; // BOX, BUBBLE_WRAP, CUSTOM
    
    @Column(name = "max_package_size")
    private String maxPackageSize; // Format: "LxWxH en cm"
    
    @Column(name = "includes_labeling")
    private Boolean includesLabeling = true;
    
    @Column(name = "includes_documentation")
    private Boolean includesDocumentation = true;
    
    public PackagingMachine() {
        super();
        setName("Packaging Machine");
        setDescription("Packages finished products for shipping");
        // Pas d'output car c'est la dernière étape
        setOutputId(null);
    }
    
    // Getters et Setters
    public String getPackagingType() {
        return packagingType;
    }
    
    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }
    
    public String getMaxPackageSize() {
        return maxPackageSize;
    }
    
    public void setMaxPackageSize(String maxPackageSize) {
        this.maxPackageSize = maxPackageSize;
    }
    
    public Boolean getIncludesLabeling() {
        return includesLabeling;
    }
    
    public void setIncludesLabeling(Boolean includesLabeling) {
        this.includesLabeling = includesLabeling;
    }
    
    public Boolean getIncludesDocumentation() {
        return includesDocumentation;
    }
    
    public void setIncludesDocumentation(Boolean includesDocumentation) {
        this.includesDocumentation = includesDocumentation;
    }
}