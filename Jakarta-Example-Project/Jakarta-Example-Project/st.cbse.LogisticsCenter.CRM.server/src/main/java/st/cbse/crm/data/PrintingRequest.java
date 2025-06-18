package st.cbse.crm.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class PrintingRequest {
    @Id
    private UUID id = UUID.randomUUID();
    private String description;

    @ManyToOne
    private Order order;

    @OneToOne(cascade = CascadeType.ALL)
    private Option option;

    protected PrintingRequest() {}
}
