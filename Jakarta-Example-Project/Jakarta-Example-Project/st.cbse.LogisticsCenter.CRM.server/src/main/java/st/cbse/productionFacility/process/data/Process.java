package st.cbse.productionFacility.process.data;

import jakarta.persistence.*;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;

import java.util.*;

@Entity
public class Process {
    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Step> steps = new ArrayList<>();

	public void setStatus(ProcessStatus active) {
		// TODO Auto-generated method stub
		
	}

	public void setSteps(List<Step> steps2) {
		// TODO Auto-generated method stub
		
	}

	public UUID getId() {
		// TODO Auto-generated method stub
		return null;
	}
}
