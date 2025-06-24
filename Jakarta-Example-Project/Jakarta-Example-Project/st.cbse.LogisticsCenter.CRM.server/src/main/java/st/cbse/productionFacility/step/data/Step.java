package st.cbse.productionFacility.step.data;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "steps")
public class Step {

    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "machine_id")
    private UUID machineId;

    @ElementCollection
    @CollectionTable(name = "step_parameters", joinColumns = @JoinColumn(name = "step_id"))
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    private Map<String, String> parameters = new HashMap<>();

    protected Step() {}

    public Step(StepType type, UUID processId, Map<String, String> params) {
        this.type = type;
        this.processId = processId;
        if (params != null) {
            this.parameters.putAll(params);
        }
    }

    public UUID getId() { return id; }

    public StepType getType() { return type; }

    public void setType(StepType type) { this.type = type; }

    public StepStatus getStatus() { return status; }

    public void setStatus(StepStatus status) { this.status = status; }

    public UUID getProcessId() { return processId; }

    public void setProcessId(UUID processId) { this.processId = processId; }

    public UUID getMachineId() { return machineId; }

    public void setMachineId(UUID machineId) { this.machineId = machineId; }

    public Map<String, String> getParameters() { return parameters; }
}