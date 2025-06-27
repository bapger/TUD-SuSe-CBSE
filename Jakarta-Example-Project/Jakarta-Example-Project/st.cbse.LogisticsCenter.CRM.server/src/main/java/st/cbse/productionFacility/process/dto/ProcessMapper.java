package st.cbse.productionFacility.process.dto;

import st.cbse.productionFacility.process.data.Process;
import st.cbse.productionFacility.process.data.ProcessStep;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessMapper implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2535321210613113194L;

	private ProcessMapper() {}
    
    public static ProcessDTO toDTO(Process process) {
        if (process == null) return null;
        
        ProcessDTO dto = new ProcessDTO();
        dto.setId(process.getId());
        dto.setStatus(process.getStatus().name());
        dto.setPrintRequestId(process.getPrintRequestId());
        dto.setCurrentStepIndex(process.getCurrentStepIndex());
        
        List<ProcessStepDTO> stepDTOs = process.getSteps().stream()
                .map(ProcessMapper::toStepDTO)
                .collect(Collectors.toList());
        dto.setSteps(stepDTOs);
        
        long completedSteps = process.getSteps().stream()
                .filter(ProcessStep::isCompleted)
                .count();
        double progress = process.getSteps().isEmpty() ? 0 : 
                (completedSteps * 100.0) / process.getSteps().size();
        dto.setProgressPercentage(progress);
        
        return dto;
    }
    
    public static ProcessStepDTO toStepDTO(ProcessStep step) {
        if (step == null) return null;
        
        ProcessStepDTO dto = new ProcessStepDTO();
        dto.setId(step.getId());
        dto.setStepType(step.getStepType());
        dto.setStepOrder(step.getStepOrder());
        dto.setCompleted(step.isCompleted());
        dto.setAssignedMachineId(step.getAssignedMachineId());
        
        return dto;
    }
    
    public static List<ProcessDTO> toDTOList(List<Process> processes) {
        return processes.stream()
                .map(ProcessMapper::toDTO)
                .collect(Collectors.toList());
    }
}