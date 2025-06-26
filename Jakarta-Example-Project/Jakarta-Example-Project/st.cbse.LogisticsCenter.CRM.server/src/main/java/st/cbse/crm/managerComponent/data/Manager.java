package st.cbse.crm.managerComponent.data;

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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Object getPassword() {
		return null;
	}
}

