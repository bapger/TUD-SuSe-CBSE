package st.cbse.productionFacility.process.interfaces;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;

import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;
import st.cbse.productionFacility.process.dto.ProcessData;

@Local
public interface IProcessMgmt {
    boolean setStatus(UUID processId, ProcessStatus status);
    List<ProcessData> viewProcesses();
    boolean addProcess(PrintRequestDTO	 req);
    boolean processStep(UUID stepId);
}