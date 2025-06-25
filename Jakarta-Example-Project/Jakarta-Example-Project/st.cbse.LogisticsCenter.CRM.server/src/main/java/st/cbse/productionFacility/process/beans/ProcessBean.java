package st.cbse.productionFacility.process.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.productionFacility.process.data.*;
import st.cbse.productionFacility.process.data.Process;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;

import java.util.*;

@Stateless
class ProcessBean {
    @PersistenceContext
    private EntityManager em;

    public UUID startProcess(List<ProcessStep> steps) {
        Process p = new Process();
        p.setStatus(ProcessStatus.ACTIVE);
        p.setSteps(steps);
        em.persist(p);
        return p.getId();
    }
}
