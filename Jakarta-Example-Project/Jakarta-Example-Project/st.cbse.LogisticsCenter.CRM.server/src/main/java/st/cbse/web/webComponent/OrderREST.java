package st.cbse.web.webComponent;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import st.cbse.crm.orderComponent.data.*;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;;

@Path("calculator")
public class OrderREST {

    @EJB
    IOrderMgmt orderMgmt;

    @GET
    @Path("result")
    @Produces("application/json")
    public JsonObject result() {
        return transform2json(orderMgmt.);
    }

    private JsonObject transform2json(Order result) {
        return Json.createObjectBuilder()
                .add("value", result.getValue())
                .add("sequence", result.getSequence())
                .build();
    }

}