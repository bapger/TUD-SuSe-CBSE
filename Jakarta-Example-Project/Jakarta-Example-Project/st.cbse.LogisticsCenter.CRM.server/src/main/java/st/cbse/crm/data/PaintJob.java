package st.cbse.crm.data;

import jakarta.persistence.Entity;

@Entity
public class PaintJob extends Option {
    private String color;

    public PaintJob() {}
    public PaintJob(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }
}
