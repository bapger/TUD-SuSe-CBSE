package st.cbse.productionFacility.productionManagerComponent.interfaces;

import java.util.List;

import jakarta.ejb.Remote;
import st.cbse.productionFacility.process.dto.ProcessDTO;

@Remote
public interface IProductionManagerMgmt {

    String loginProductionManager(String email, String password);

    List<ProcessDTO> getAllProcesses();

}
