package st.cbse.crm.orderComponent.data;

import jakarta.persistence.Entity;

@Entity
public class PaintJob extends Option {
    private String color;

    public PaintJob(String colour, int layers) {}

}
