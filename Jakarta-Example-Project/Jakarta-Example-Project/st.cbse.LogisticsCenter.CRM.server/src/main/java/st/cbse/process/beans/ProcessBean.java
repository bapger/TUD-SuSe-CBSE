package st.cbse.process.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import st.cbse.process.data.*;
import st.cbse.process.data.Process;
import st.cbse.process.data.enums.ProcessStatus;

import java.util.*;

@Stateless
class ProcessBean {
    @PersistenceContext
    private EntityManager em;

    public UUID startProcess(List<Step> steps) {
        Process p = new Process();
        p.setStatus(ProcessStatus.ACTIVE);
        p.setSteps(steps);
        em.persist(p);
        return p.getId();
    }
}
