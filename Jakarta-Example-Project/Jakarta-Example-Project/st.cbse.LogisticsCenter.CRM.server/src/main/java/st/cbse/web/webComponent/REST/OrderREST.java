package st.cbse.web.webComponent.REST;

import java.util.List;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.FormParam;
import st.cbse.crm.orderComponent.data.*;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.crm.dto.OptionDTO;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;

@Path("orders")
public class OrderREST {

    @EJB
    IOrderMgmt orderMgmt;


    @GET
    @Path("getOrders")
    @Produces("application/json")
    public JsonObject getAllOrders() {
        return convertOrderDTOListToJson(orderMgmt.fetchAllOrderDTOs(),orderMgmt);
    }


    @GET
    @Path("getOrdersByCustomer/{customerId}")
    @Produces("application/json")
    public JsonObject getOrdersByCustomer(@jakarta.ws.rs.PathParam("customerId") String customerIdStr) {
        UUID customerId;
        try {
            customerId = UUID.fromString(customerIdStr);
        } catch (IllegalArgumentException e) {
            return Json.createObjectBuilder()
                    .add("error", "Invalid UUID format for customerId.")
                    .build();
        }

        List<OrderDTO> customerOrders = orderMgmt.getOrdersByCustomer(customerId);
        return convertOrderDTOListToJson(customerOrders,orderMgmt);
    }


    @POST
    @Path("create")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/plain")
    public Response createOrder(
        @FormParam("customerId") String customerIdStr,
        @FormParam("price") String priceStr) {

        try {
            UUID customerId = UUID.fromString(customerIdStr);
            BigDecimal price = new BigDecimal(priceStr);
            UUID orderId = orderMgmt.createOrder(customerId, price);
            return Response.ok(orderId.toString()).build();
        } catch (Exception e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }
    }
    @POST
    @Path("addPrintRequest")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/plain")
    public Response addPrintRequest(
        @FormParam("orderId") String orderIdStr,
        @FormParam("stlPath") String stlPath,
        @FormParam("note") String note) {

        try {
            UUID orderId = UUID.fromString(orderIdStr);
            UUID requestId = orderMgmt.addPrintRequest(orderId, stlPath, note);
            return Response.ok(requestId.toString()).build();
        } catch (Exception e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }
    }
    @POST
    @Path("addPaintOption")
    @Consumes("application/x-www-form-urlencoded")
    public Response addPaintOption(
        @FormParam("requestId") String requestIdStr,
        @FormParam("color") String color,
        @FormParam("layers") int layers) {

        try {
            UUID requestId = UUID.fromString(requestIdStr);
            orderMgmt.addPaintJobOption(requestId, color, layers);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }
    }
    @POST
    @Path("addSmoothingOption")
    @Consumes("application/x-www-form-urlencoded")
    public Response addSmoothingOption(
        @FormParam("requestId") String requestIdStr,
        @FormParam("granularity") String granularity) {

        try {
            UUID requestId = UUID.fromString(requestIdStr);
            orderMgmt.addSmoothingOption(requestId, granularity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }
    }
    @POST
    @Path("addEngravingOption")
    @Consumes("application/x-www-form-urlencoded")
    public Response addEngravingOption(
        @FormParam("requestId") String requestIdStr,
        @FormParam("text") String text,
        @FormParam("font") String font,
        @FormParam("image") String image) {

        try {
            UUID requestId = UUID.fromString(requestIdStr);
            orderMgmt.addEngravingOption(requestId, text, font, image);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }
    }
    @POST
    @Path("finalize")
    @Consumes("application/x-www-form-urlencoded")
    public Response finalizeOrder(@FormParam("orderId") String orderIdStr) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            orderMgmt.finalizeOrder(orderId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static JsonObject convertOrderDTOListToJson(List<OrderDTO> orders,IOrderMgmt orderMgmt) {

        JsonArrayBuilder orderArrayBuilder = Json.createArrayBuilder();

        for (OrderDTO orderDTO : orders) {
            JsonObjectBuilder orderObjectBuilder = Json.createObjectBuilder();

            orderObjectBuilder.add("id", orderDTO.getId().toString());
            orderObjectBuilder.add("status", orderDTO.getStatus());
            orderObjectBuilder.add("customerName", orderDTO.getCustomerName());
            orderObjectBuilder.add("creationDate", orderDTO.getCreationDate().format(DATE_TIME_FORMATTER));
            orderObjectBuilder.add("total", orderDTO.getTotal());
            try {
				orderObjectBuilder.add("hasUnpaidInvoice", orderMgmt.hasUnpaidInvoice(orderDTO.getId()));
			} catch (Exception e) {

			}

            JsonArrayBuilder printRequestsArrayBuilder = Json.createArrayBuilder();
            for (PrintRequestDTO printRequestDTO : orderDTO.getPrintingRequests()) {
                printRequestsArrayBuilder.add(printingRequestToJsonObject(printRequestDTO));
            }
            orderObjectBuilder.add("printingRequests", printRequestsArrayBuilder);

            orderArrayBuilder.add(orderObjectBuilder);
        }

        return Json.createObjectBuilder()
                .add("orders", orderArrayBuilder)
                .build();
    }

    public static JsonObject printingRequestToJsonObject(PrintRequestDTO printRequestDTO) {
        return Json.createObjectBuilder()
                .add("ID", printRequestDTO.getId().toString())
                .add("options", convertOptionDTOListToJson(printRequestDTO.getOptions()))
                .add("price", printRequestDTO.getPrice())
                .add("note", printRequestDTO.getNote())
                .build();
    }

    public static JsonObject convertOptionDTOListToJson(List<OptionDTO> optionDTOs) {
        JsonArrayBuilder optionArrayBuilder = Json.createArrayBuilder();

        for (OptionDTO optionDTO : optionDTOs) {
            JsonObjectBuilder optionObjectBuilder = Json.createObjectBuilder()
                    .add("type", optionDTO.getType())
                    .add("price", optionDTO.getPrice());
            optionArrayBuilder.add(optionObjectBuilder);
        }

        return Json.createObjectBuilder()
                .add("options", optionArrayBuilder)
                .build();
    }

	public static Object createError(String message) {
		return null;
	}

}