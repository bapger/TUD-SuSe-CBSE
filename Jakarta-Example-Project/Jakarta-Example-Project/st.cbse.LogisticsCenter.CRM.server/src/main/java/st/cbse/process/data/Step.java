package st.cbse.process.data;

import jakarta.persistence.*;

@Entity
public class Step {
    @Id
    @GeneratedValue
    private Long id;

    private String type; // Packaging, Painting, etc.
}
