package st.cbse.web.webComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    public static JsonObject convertOrderDTOListToJson(List<OrderDTO> orders) {
        JsonArrayBuilder orderArrayBuilder = Json.createArrayBuilder();

        for (OrderDTO orderDTO : orders) {
            JsonObjectBuilder orderObjectBuilder = Json.createObjectBuilder();

            orderObjectBuilder.add("id", orderDTO.getId().toString()); // UUID en String
            orderObjectBuilder.add("status", orderDTO.getStatus());
            orderObjectBuilder.add("customerName", orderDTO.getCustomerName());
            // orderObjectBuilder.add("creationDate", orderDTO.getCreationDate()); //
            // LocalDateTime
            orderObjectBuilder.add("total", orderDTO.getTotal()); // BigDecimal est géré directement

            /*
             * Gérer la liste de PrintRequestDTO
             * JsonArrayBuilder printRequestsArrayBuilder = Json.createArrayBuilder();
             * for (PrintRequestDTO printRequestDTO : orderDTO.getPrintingRequests()) {
             * // Supposons que PrintRequestDTO a une méthode toJsonObject()
             * printRequestsArrayBuilder.add(printRequestDTO.toJsonObject());
             * }
             * //orderObjectBuilder.add("printingRequests", printRequestsArrayBuilder);
             */

            orderArrayBuilder.add(orderObjectBuilder);
        }

        return Json.createObjectBuilder()
                .add("orders", orderArrayBuilder)
                .build();
    }

    /**
     * Méthode utilitaire pour convertir un seul OrderDTO en JsonObject.
     * Utile si vous avez besoin de convertir des commandes individuellement.
     *
     * @param orderDTO L'objet OrderDTO à convertir.
     * @return Un JsonObject représentant la commande.
     */
    public static JsonObject convertOrderDTOToJson(OrderDTO orderDTO) {
        JsonObjectBuilder orderObjectBuilder = Json.createObjectBuilder();

        orderObjectBuilder.add("id", orderDTO.getId().toString()); // UUID en String
        orderObjectBuilder.add("status", orderDTO.getStatus());
        orderObjectBuilder.add("customerName", orderDTO.getCustomerName());
        // orderObjectBuilder.add("creationDate", orderDTO.getCreationDate()); //
        // LocalDateTime
        // en String
        orderObjectBuilder.add("total", orderDTO.getTotal()); // BigDecimal est géré directement

        /*
         * // Gérer la liste de PrintRequestDTO
         * JsonArrayBuilder printRequestsArrayBuilder = Json.createArrayBuilder();
         * for (PrintRequestDTO printRequestDTO : orderDTO.getPrintingRequests()) {
         * // Supposons que PrintRequestDTO a une méthode toJsonObject()
         * printRequestsArrayBuilder.add(printRequestDTO.toJsonObject());
         * }
         * orderObjectBuilder.add("printingRequests", printRequestsArrayBuilder);
         */

        return orderObjectBuilder.build();
    }

}