package st.cbse.crm.customerComponent.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "CUSTOMER",
uniqueConstraints = @UniqueConstraint(name = "UK_CUSTOMER_EMAIL",
columnNames = "email"))
public class Customer {
    @Id
    private UUID id = UUID.randomUUID();
    private String name;
    @Id
    private String email;
    private String password;

    @Embedded
    private Address address;

    protected Customer() {}

    public Customer(String name, String email, String password, Address address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
}
