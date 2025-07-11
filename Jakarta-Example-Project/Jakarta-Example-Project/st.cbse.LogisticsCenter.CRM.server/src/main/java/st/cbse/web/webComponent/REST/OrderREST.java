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

    // Access html form :
    // file:///H:/Documents/GitHub/TUD-SuSe-CBSE/Jakarta-Example-Project/Jakarta-Example-Project/st.cbse.LogisticsCenter.CRM.server/src/main/webapp/createOrderForm.html

    @POST
    @Path("createOrderForm")
    @Consumes("application/x-www-form-urlencoded")
    public String createOrderForm(
            @FormParam("customerId") String customerIdStr,
            @FormParam("price") String priceStr,
            @FormParam("stl") List<String> stlFiles,
            @FormParam("note") List<String> notes,
            @FormParam("optionType") List<String> optionTypes,
            @FormParam("optionData1") List<String> optionData1,
            @FormParam("optionData2") List<String> optionData2) {
        UUID customerId = UUID.fromString(customerIdStr);
        BigDecimal price = new BigDecimal(priceStr);

        UUID orderId = orderMgmt.createOrder(customerId, price);
        System.out.println(stlFiles);
        for (int i = 0; i < stlFiles.size(); i++) {
            String stl = stlFiles.get(i);
            String note = notes.get(i);

            UUID requestId = orderMgmt.addPrintRequest(orderId, stl, note);

            String optType = optionTypes.get(i);
            String data1 = optionData1.get(i);
            String data2 = optionData2.get(i);

            switch (optType.toLowerCase()) {
                case "paint":
                    orderMgmt.addPaintJobOption(requestId, data1, Integer.parseInt(data2));
                    break;
                case "smooth":
                    orderMgmt.addSmoothingOption(requestId, data1);
                    break;
                case "engrave":
                    orderMgmt.addEngravingOption(requestId, data1, data2, "");
                    break;
            }
        }

        orderMgmt.finalizeOrder(orderId);
        return "Order created: " + orderId;
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