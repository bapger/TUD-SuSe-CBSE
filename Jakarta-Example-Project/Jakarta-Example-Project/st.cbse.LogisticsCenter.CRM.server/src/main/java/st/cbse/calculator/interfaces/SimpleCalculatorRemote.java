package st.cbse.calculator.interfaces;

import jakarta.ejb.Remote;
import st.cbse.calculator.data.Result;

@Remote
public interface SimpleCalculatorRemote {

	public Result start(int a);
	public Result add(int a);
	public Result sub(int a);
	public Result result();
	
}
