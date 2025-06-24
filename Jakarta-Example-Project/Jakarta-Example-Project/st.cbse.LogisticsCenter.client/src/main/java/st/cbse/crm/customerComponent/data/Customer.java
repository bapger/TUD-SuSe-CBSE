package st.cbse.crm.customerComponent.data;

import java.util.UUID;

public class Customer {
    private UUID id = UUID.randomUUID();
    private String name;
    private String email;
    private String password;
    private Address address;


    public Customer(String name, String email, String password, Address address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
}
