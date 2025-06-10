package st.cbse.calculator.beans;

import java.util.List;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import st.cbse.calculator.data.Result;
import st.cbse.calculator.interfaces.SimpleCalculatorRemote;

@Stateless
@LocalBean
public class SimpleCalculator implements SimpleCalculatorRemote {


	@PersistenceContext
	EntityManager em;
	

	@Override
	public Result start(int a) {
		Result r = new Result(a, "" + a);
		em.persist(r);
		return r;
	}

	@Override
	public Result add(int a) {
		Result r = getLatestResult();
		r.setValue(r.getValue() + a);
		r.setSequence(r.getSequence() + " + " + a);
		em.persist(r);
		return r;
	}

	@Override
	public Result sub(int a) {
		Result r = getLatestResult();
		r.setValue(r.getValue() - a);
		r.setSequence(r.getSequence() + " - " + a);
		em.persist(r);
		return r;
	}

	@Override
	public Result result() {
		Result r = getLatestResult();
		return r;
	}
	

	private Result getLatestResult() {
		TypedQuery<Result> query= em.createQuery("SELECT r FROM Result r", Result.class);
		List<Result> list = query.getResultList();
		if(list.size() == 0) {
			return null;
		}else {
			return list.get(list.size()-1);
		}
	}

}
