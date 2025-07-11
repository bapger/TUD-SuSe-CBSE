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

    // Get all orders : Manager only
    @GET
    @Path("getOrders")
    @Produces("application/json")
    public JsonObject getAllOrders() {
        return convertOrderDTOListToJson(orderMgmt.fetchAllOrderDTOs());
    }

    // get customer's orders : Customer and Manager
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
        return convertOrderDTOListToJson(customerOrders);
    }

    @POST
    @Path("createOrderForm")
    // Access html form :
    // file:///H:/Documents/GitHub/TUD-SuSe-CBSE/Jakarta-Example-Project/Jakarta-Example-Project/st.cbse.LogisticsCenter.CRM.server/src/main/webapp/createOrderForm.html
    @Consumes("application/x-www-form-urlencoded")
    public String createOrderForm(
            @FormParam("customerId") String customerIdStr,
            @FormParam("price") String priceStr) {
        UUID customerId = UUID.fromString(customerIdStr);
        BigDecimal price = new BigDecimal(priceStr);

        String msg = "Order created" + orderMgmt.createOrder(customerId, price);
        return msg;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static JsonObject convertOrderDTOListToJson(List<OrderDTO> orders) {

        JsonArrayBuilder orderArrayBuilder = Json.createArrayBuilder();

        for (OrderDTO orderDTO : orders) {
            JsonObjectBuilder orderObjectBuilder = Json.createObjectBuilder();

            orderObjectBuilder.add("id", orderDTO.getId().toString());
            orderObjectBuilder.add("status", orderDTO.getStatus());
            orderObjectBuilder.add("customerName", orderDTO.getCustomerName());
            orderObjectBuilder.add("creationDate", orderDTO.getCreationDate().format(DATE_TIME_FORMATTER));
            orderObjectBuilder.add("total", orderDTO.getTotal());

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

}