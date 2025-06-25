package st.cbse.crm.orderComponent.data;

import jakarta.persistence.Entity;

@Entity
public class PaintJob extends Option {
    private String color;
    private int layers;
    
    protected PaintJob() {}

    public PaintJob(String color, int layers) {
    	this.color = color;
    	this.layers = layers;
    }

}
