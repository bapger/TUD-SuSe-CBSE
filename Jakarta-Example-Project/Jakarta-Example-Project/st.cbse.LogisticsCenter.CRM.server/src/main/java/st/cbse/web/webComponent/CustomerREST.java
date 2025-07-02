package st.cbse.web.webComponent;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import st.cbse.crm.customerComponent.data.Customer;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;;

@Path("calculator")
public class CustomerREST {

    @EJB
    ICustomerMgmt customerMgmt;

    @GET
    @Path("result")
    @Produces("application/json")
    public JsonObject result() {
        return transform2json(customerMgmt.result());
    }

    private JsonObject transform2json(Result result) {
        return Json.createObjectBuilder()
                .add("value", result.getValue())
                .add("sequence", result.getSequence())
                .build();
    }

}