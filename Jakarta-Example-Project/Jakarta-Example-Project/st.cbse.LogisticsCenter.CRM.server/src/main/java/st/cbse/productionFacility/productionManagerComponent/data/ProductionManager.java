package st.cbse.productionFacility.productionManagerComponent.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProductionManager {
    @Id
    private String email;
    private String password;

    public ProductionManager() {
    }

    public ProductionManager(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
