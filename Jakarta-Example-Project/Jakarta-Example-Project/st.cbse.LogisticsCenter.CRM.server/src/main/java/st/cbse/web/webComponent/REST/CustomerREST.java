package st.cbse.web.webComponent.REST;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerREST {

    @EJB
    private ICustomerMgmt customerMgmt;

    @EJB
    private IOrderMgmt orderMgmt;

    // Register new customer
    @POST
    @Path("register")
    public Response registerCustomer(JsonObject input) {
        try {
            String name = input.getString("name");
            String email = input.getString("email");
            String password = input.getString("password");

            UUID customerId = customerMgmt.registerCustomer(name, email, password);

            JsonObject response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("customerId", customerId.toString())
                    .add("message", "Customer registered successfully")
                    .build();

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error", e.getMessage())
                    .build();
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    // Login customer
    @POST
    @Path("login")
    public Response loginCustomer(JsonObject input) {
        try {
            String email = input.getString("email");
            String password = input.getString("password");

            UUID customerId = customerMgmt.loginCustomer(email, password);

            JsonObject response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("customerId", customerId.toString())
                    .add("token", "customer_" + customerId.toString()) // Simple token
                    .build();

            return Response.ok(response).build();

        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error", "Invalid credentials")
                    .build();
            return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
        }
    }

    // Get customer orders
    @GET
    @Path("{customerId}/orders")
    public Response getOrderHistory(@PathParam("customerId") String customerIdStr) {
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            List<OrderDTO> orders = orderMgmt.getOrdersByCustomer(customerId);

            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("count", orders.size());

            // Convert orders to JSON (simplified version)
            // In real implementation, you'd convert OrderDTO to JSON properly
            response.add("orders", Json.createArrayBuilder());

            return Response.ok(response.build()).build();

        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error", e.getMessage())
                    .build();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    // Create new order
    @POST
    @Path("{customerId}/orders")
    public Response createOrder(@PathParam("customerId") String customerIdStr,
            JsonObject input) {
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            BigDecimal basePrice = new BigDecimal(input.getString("basePrice"));

            UUID orderId = orderMgmt.createOrder(customerId, basePrice);

            JsonObject response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("orderId", orderId.toString())
                    .build();

            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();

        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error", e.getMessage())
                    .build();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build(); // <-- parenthèse et build manquants
        }
    }
}