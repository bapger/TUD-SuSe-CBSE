package st.cbse.web.webComponent.REST;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.*;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;
import st.cbse.productionFacility.process.dto.ProcessDTO;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;
import st.cbse.productionFacility.productionManagerComponent.interfaces.IProductionManagerMgmt;


@Path("/production")
@Produces(MediaType.APPLICATION_JSON)
public class ProdManagerREST {

    @EJB
    IProductionManagerMgmt productionManagerMgmt;

    @EJB
    IProcessMgmt processMgmt;

    // === Login ===
    @POST
    @Path("/prodManager/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(
            @FormParam("email") String email,
            @FormParam("password") String password) {

        try {
            String prodManagerId = productionManagerMgmt.loginProductionManager(email, password);
            return Response.seeOther(URI.create("../prodManager-dashboard.html")).build();
        } catch (Exception e) {
            return Response.seeOther(
                URI.create("../login.html?role=prodManager&error=invalid")
            ).build();
        }
    }


    @GET
    @Path("/processes")
    public Response getAllProcesses() {
        try {
            List<ProcessDTO> processes = productionManagerMgmt.getAllProcesses();
            return Response.ok(convertProcessListToJson(processes)).build();
        } catch (Exception e) {
            return Response.serverError().entity(createError(e.getMessage())).build();
        }
    }

    @POST
    @Path("/processes/{id}/pause")
    public Response pause(@PathParam("id") UUID id) {
        processMgmt.pauseProcess(id);
        return Response.ok().build();
    }

    @POST
    @Path("/processes/{id}/resume")
    public Response resume(@PathParam("id") UUID id) {
        processMgmt.resumeProcess(id);
        return Response.ok().build();
    }

    @POST
    @Path("/processes/{id}/cancel")
    public Response cancel(@PathParam("id") UUID id) {
        processMgmt.cancelProcess(id);
        return Response.ok().build();
    }

    private JsonObject convertProcessListToJson(List<ProcessDTO> processes) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (ProcessDTO p : processes) {
            array.add(Json.createObjectBuilder()
                .add("id", p.getId().toString())
                .add("status", p.getStatus().toString())
                .add("progressPercentage", p.getProgressPercentage()));
        }
        return Json.createObjectBuilder().add("processes", array).build();
    }

    private JsonObject createError(String msg) {
        return Json.createObjectBuilder().add("error", msg).build();
    }
}

