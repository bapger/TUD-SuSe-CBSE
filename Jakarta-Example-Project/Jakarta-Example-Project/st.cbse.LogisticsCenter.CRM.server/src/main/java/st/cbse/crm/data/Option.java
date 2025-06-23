package st.cbse.crm.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "option_type")
public abstract class Option {
    @Id
    protected UUID id = UUID.randomUUID();
    
    @ManyToOne
    private PrintingRequest printingRequest;


    protected String name;
    protected String description;

    public UUID getId() {
        return id;
    }
}
