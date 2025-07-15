package st.cbse.web.webComponent.REST;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.shipment.interfaces.IShipmentMgmt;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.productionFacility.storage.dto.FinishedProductsDto;
import st.cbse.productionFacility.storage.dto.ItemInfo;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Path("/manager")
@Produces(MediaType.APPLICATION_JSON)
public class ManagerREST {

    @EJB
    IManagerMgmt managerMgmt;

    @EJB
    IShipmentMgmt shipmentMgmt;

    @EJB
    IOrderMgmt orderMgmt;

    @EJB
    IStorageMgmt storageMgmt;

    // ========== Login ==========
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(
            @FormParam("email") String email,
            @FormParam("password") String password) {
        try {
            String managerId = managerMgmt.loginManager(email, password);
            return Response.seeOther(URI.create("../manager-dashboard.html")).build();
        } catch (Exception e) {
            return Response.seeOther(URI.create("../login.html?role=manager&error=invalid")).build();
        }
    }

    // ========== Get All Orders ==========
    @GET
    @Path("/orders")
    public Response getAllOrders() {
        try {
            List<OrderDTO> orders = managerMgmt.listAllOrders();
            JsonObject json = OrderREST.convertOrderDTOListToJson(orders);
            return Response.ok(json).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(OrderREST.createError(e.getMessage()))
                    .build();
        }
    }

    // ========== Add Note to Print Request ==========
    @POST
    @Path("/addNote/{requestId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addNote(
            @PathParam("requestId") UUID requestId,
            @FormParam("note") String note) {
        try {
            managerMgmt.addNoteToRequest(requestId, note);
            return Response.ok("Note added").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("ERROR: " + e.getMessage()).build();
        }
    }


    // ========== Send Order to Production ==========
    @POST
    @Path("/sendToProduction/{orderId}")
    public Response sendToProduction(@PathParam("orderId") UUID orderId) {
        try {
            managerMgmt.sendPrintToProd(orderId);
            return Response.ok("Order sent to production").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("ERROR: " + e.getMessage()).build();
        }
    }

    // ========== Ship Order & Create Invoice ==========
    @POST
    @Path("/ship/{orderId}")
    public Response shipOrder(@PathParam("orderId") UUID orderId) {
        try {
            shipmentMgmt.shipOrder(orderId);
            orderMgmt.createInvoiceForShippedOrder(orderId);
            return Response.ok("Order shipped and invoice created").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("ERROR: " + e.getMessage()).build();
        }
    }

    // ========== View Finished Products in Storage ==========
    @GET
    @Path("/storage")
    public Response getStorage() {
        try {
            List<FinishedProductsDto> items = storageMgmt.getAllFinishedProducts();
            if (items == null) {
                System.err.println("Storage items is null");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(OrderREST.createError("Storage items null"))
                    .build();
            }
            System.out.println("Storage items count: " + items.size());
            return Response.ok(convertFinishedProductsToJson(items)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(OrderREST.createError(e.getMessage()))
                    .build();
        }
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static JsonObject convertFinishedProductsToJson(List<FinishedProductsDto> items) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (FinishedProductsDto item : items) {
            JsonObjectBuilder itemBuilder = Json.createObjectBuilder()
                .add("id", item.getId().toString())
                .add("processId", item.getProcessId().toString())
                .add("printRequestId", item.getPrintRequestId().toString())
//                .add("orderId", item.getOrderId().toString())
                .add("completedAt", item.getCompletedAt().format(DATE_TIME_FORMATTER))
                .add("shipped", item.isShipped());

            if (item.getItemInfo() != null) {
                itemBuilder.add("itemInfo", convertItemInfo(item.getItemInfo()));
            }

            arrayBuilder.add(itemBuilder);
        }

        return Json.createObjectBuilder()
                .add("items", arrayBuilder)
                .build();
    }

    public static JsonObject convertItemInfo(ItemInfo info) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", info.getId().toString());
        builder.add("processId", info.getProcessId().toString());
        builder.add("printRequestId", info.getPrintRequestId().toString());
        builder.add("currentLocation", info.getCurrentLocation() != null ? info.getCurrentLocation() : "Unknown");
        builder.add("status", info.getStatus() != null ? info.getStatus() : "Unknown");

        if (info.getCreatedAt() != null) {
            builder.add("createdAt", info.getCreatedAt().format(DATE_TIME_FORMATTER));
        }

        return builder.build();
    }

}