package st.cbse.web.webComponent.REST;

import jakarta.ejb.EJB;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Response;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;

@Path("manager")
public class ManagerREST {
    
    @EJB
    IManagerMgmt managerMgmt;
    
    @POST
    @Path("login")
    @Consumes("application/x-www-form-urlencoded")
    public Response login(
            @FormParam("email") String email,
            @FormParam("password") String password) {
        
        try {
            String managerId = managerMgmt.loginManager(email, password);
            // Rediriger vers le dashboard manager
            return Response.seeOther(
                java.net.URI.create("../manager-dashboard.html")
            ).build();
        } catch (Exception e) {
            // Rediriger vers login avec message d'erreur
            return Response.seeOther(
                java.net.URI.create("../login.html?role=manager&error=invalid")
            ).build();
        }
    }
}