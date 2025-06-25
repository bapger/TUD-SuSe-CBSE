package st.cbse.productionFacility.step.interfaces;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.ejb.Local;
import st.cbse.productionFacility.step.data.Step;
import st.cbse.productionFacility.step.data.StepType;

@Local
public interface IStepMgmt {
    boolean addStep(StepType type, UUID processId, Map<String, String> parameters);
    boolean executeStep(UUID stepId);
    boolean completeStep(UUID stepId);
    Step getStep(UUID stepId);
    List<Step> getStepsByProcess(UUID processId);
    List<Step> getPendingSteps();
}