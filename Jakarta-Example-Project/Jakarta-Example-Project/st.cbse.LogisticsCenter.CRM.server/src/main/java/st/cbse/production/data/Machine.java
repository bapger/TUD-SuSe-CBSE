package st.cbse.production.data;

import jakarta.persistence.*;
import java.util.UUID;
import st.cbse.production.data.enums.MachineStatus;

@Entity
public class Machine {
    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    private MachineStatus status;

    public Machine() {
        this.status = MachineStatus.AVAILABLE;
    }

    public void activate() {
        this.status = MachineStatus.ACTIVE;
    }
}
