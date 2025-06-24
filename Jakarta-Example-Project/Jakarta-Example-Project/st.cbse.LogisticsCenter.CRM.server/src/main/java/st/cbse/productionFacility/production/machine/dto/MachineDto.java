package st.cbse.productionFacility.production.machine.dto;

import java.util.UUID;

import st.cbse.productionFacility.production.machine.data.Machine;
import st.cbse.productionFacility.production.machine.data.MachineStatus;

public class MachineDto {

    private UUID id;
    private String machineType;
    private MachineStatus status;
    private UUID currentProductId;
    private String name;

    public MachineDto() {
    }

    public MachineDto(UUID id,String machineType,MachineStatus status,UUID currentProductId,String name) {
        this.id = id;
        this.machineType = machineType;
        this.status = status;
        this.currentProductId = currentProductId;
        this.name = name;
    }

    public static MachineDto fromEntity(Machine machine) {
        return new MachineDto(
                machine.getId(),
                machine.getClass().getSimpleName(),
                machine.getStatus(),
                machine.getCurrentProductId(),
                machine.getName()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    public UUID getCurrentProductId() {
        return currentProductId;
    }

    public void setCurrentProductId(UUID currentProductId) {
        this.currentProductId = currentProductId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}