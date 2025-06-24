package st.cbse.productionFacility.process.interfaces;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import st.cbse.productionFacility.process.data.ProcessStatus;
import st.cbse.productionFacility.process.data.ProcessData;
import st.cbse.productionFacility.request.PrintingRequest;

@Local
public interface IProcessMgmt {
    boolean setStatus(UUID processId, ProcessStatus status);
    List<ProcessData> viewProcesses();
    boolean addProcess(PrintingRequest req);
    boolean processStep(UUID stepId);
}