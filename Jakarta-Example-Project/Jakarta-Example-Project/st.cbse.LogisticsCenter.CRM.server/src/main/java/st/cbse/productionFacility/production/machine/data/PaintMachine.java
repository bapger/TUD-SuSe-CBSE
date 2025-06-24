package st.cbse.productionFacility.production.machine.data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PAINT")
public class PaintMachine extends Machine {
    
    @Column(name = "paint_method")
    private String paintMethod; // SPRAY, BRUSH, DIP
    
    @Column(name = "current_color")
    private String currentColor;
    
    @Column(name = "color_capacity")
    private Integer colorCapacity; // Nombre de couleurs disponibles
    
    public PaintMachine() {
        super();
        setName("Paint Machine");
        setDescription("Applies paint coating to 3D printed objects");
    }
    
    // Getters et Setters
    public String getPaintMethod() {
        return paintMethod;
    }
    
    public void setPaintMethod(String paintMethod) {
        this.paintMethod = paintMethod;
    }
    
    public String getCurrentColor() {
        return currentColor;
    }
    
    public void setCurrentColor(String currentColor) {
        this.currentColor = currentColor;
    }
    
    public Integer getColorCapacity() {
        return colorCapacity;
    }
    
    public void setColorCapacity(Integer colorCapacity) {
        this.colorCapacity = colorCapacity;
    }
}