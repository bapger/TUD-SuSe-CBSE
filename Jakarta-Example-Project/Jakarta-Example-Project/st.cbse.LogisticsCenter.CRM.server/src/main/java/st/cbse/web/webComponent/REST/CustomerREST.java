package st.cbse.web.webComponent.REST;

import jakarta.ejb.EJB;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Response;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;

import java.util.UUID;

@Path("customer")
public class CustomerREST {
    
    @EJB
    ICustomerMgmt customerMgmt;
    
    @EJB
    IOrderMgmt orderMgmt;
    
    @POST
    @Path("login")
    @Consumes("application/x-www-form-urlencoded")
    public Response login(
            @FormParam("email") String email,
            @FormParam("password") String password) {
        
        try {
            UUID customerId = customerMgmt.loginCustomer(email, password);
            // Rediriger vers le dashboard customer
            return Response.seeOther(
            	    java.net.URI.create("../customer-dashboard.html?id=" + customerId)
            	).build();

        } catch (Exception e) {
            // Rediriger vers login avec message d'erreur
            return Response.seeOther(
                java.net.URI.create("../login.html?role=customer&error=invalid")
            ).build();
        }
    }
    
    @POST
    @Path("register")
    @Consumes("application/x-www-form-urlencoded")
    public Response register(
            @FormParam("fullName") String fullName,
            @FormParam("email") String email,
            @FormParam("password") String password,
            @FormParam("confirmPassword") String confirmPassword) {
        
        try {
            // Vérifier que les mots de passe correspondent
            if (!password.equals(confirmPassword)) {
                return Response.seeOther(
                    java.net.URI.create("../register.html?error=passwordMismatch")
                ).build();
            }
            
            // Enregistrer le nouveau client
            UUID customerId = customerMgmt.registerCustomer(fullName, email, password);
            
            // Rediriger vers la page de login avec message de succès
            return Response.seeOther(
                java.net.URI.create("../login.html?role=customer&success=registered")
            ).build();
            
        } catch (Exception e) {
            // Rediriger vers register avec message d'erreur
            return Response.seeOther(
                java.net.URI.create("../register.html?error=registrationFailed")
            ).build();
        }
    }
    
    @POST
    @Path("pay/{orderId}")
    @Consumes("application/x-www-form-urlencoded")
    public Response payOrder(
    	@PathParam("orderId") String orderIdStr,
        @FormParam("transactionRef") String transactionRef
    ) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
			OrderDTO order = orderMgmt.getOrderDTO(orderId);

            if (order == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Order not found").build();
            }

            if (order.getStatus() == "SHIPPED") {
                if (orderMgmt.hasUnpaidInvoice(orderId)) {
                    orderMgmt.payInvoice(orderId, transactionRef);
                    return Response.ok("Invoice paid").build();
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                                   .entity("Invoice already paid").build();
                }
            } else if (order.getStatus() =="PENDING") {
                orderMgmt.pay(orderId, transactionRef);
                return Response.ok("Order payment booked").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Order is not eligible for payment").build();
            }
        } catch (Exception e) {
            return Response.serverError()
                           .entity("Payment failed: " + e.getMessage()).build();
        }
    }

}