package st.cbse.productionFacility.step.interfaces;

import java.util.Map;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.step.data.StepType;

@Local
public interface IStepMgmt {

    boolean addStep(StepType type, UUID processId, Map<String, String> parameters);
    boolean executeStep(UUID stepId);
}