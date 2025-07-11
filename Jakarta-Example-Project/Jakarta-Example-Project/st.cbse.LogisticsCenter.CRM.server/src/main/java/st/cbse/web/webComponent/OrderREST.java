package st.cbse.web.webComponent;

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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
    public JsonObject result() {
        return convertOrderDTOListToJson(orderMgmt.fetchAllOrderDTOs());
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