package st.cbse.crm.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Manager {
    @Id private String email;
    private String password;

    public Manager() {}
    public Manager(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

