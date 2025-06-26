package st.cbse.productionFacility.process.interfaces;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Remote;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.productionFacility.process.dto.ProcessDTO;

@Remote
public interface IProcessMgmt {
    UUID createProcessFromPrintRequest(PrintRequestDTO printRequest);
    ProcessDTO getProcess(UUID processId);

    List<ProcessDTO> getAllProcesses();
    List<ProcessDTO> getProcessesByStatus(String status);

    boolean startProcess(UUID processId);
    boolean cancelProcess(UUID processId);
    boolean validateCurrentStep(UUID processId, UUID machineId);
    boolean isProcessComplete(UUID processId);
    boolean pauseProcess(UUID processId);
    boolean resumeProcess(UUID processId);
    
    void notifyStepCompleted(UUID processId, UUID machineId);
    void notifyMachineStopped(UUID processId);

    ProcessDTO getCurrentStepInfo(UUID processId);
}