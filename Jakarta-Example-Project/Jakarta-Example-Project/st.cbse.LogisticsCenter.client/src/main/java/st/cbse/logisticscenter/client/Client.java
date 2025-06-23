package st.cbse.logisticscenter.client;

import javax.naming.*;
import java.util.*;
import java.util.Scanner;

import st.cbse.crm.interfaces.ICustomerMgmt;
import st.cbse.crm.interfaces.IOrderMgmt;
import st.cbse.shipment.interfaces.IShipmentMgmt;

import java.util.UUID;

public class Client {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            System.out.println("=== Logistics System Client ===");
            InitialContext context = getContext();

            ICustomerMgmt customerMgmt = (ICustomerMgmt) context.lookup(
                "ejb:/st.cbse.LogisticsCenter.CRM.server/CustomerBean!st.cbse.crm.interfaces.ICustomerMgmt");
            IOrderMgmt orderMgmt = (IOrderMgmt) context.lookup(
                "ejb:/st.cbse.LogisticsCenter.CRM.server/OrderBean!st.cbse.crm.interfaces.IOrderMgmt");
            IShipmentMgmt shipmentMgmt = (IShipmentMgmt) context.lookup(
                "ejb:/st.cbse.LogisticsCenter.server/ShipmentBean!st.cbse.shipment.interfaces.IShipmentMgmt");

            UUID customerId = null;
            UUID orderId = null;
            

            boolean running = true;
            while (running) {
                System.out.println("\nMenu:");
                System.out.println("1. Register a customer");
                System.out.println("2. Create an order");
                System.out.println("3. Ship an order");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                String input = scanner.nextLine();

                switch (input) {
                    case "1":
                        System.out.println("\n[Register Customer]");
                        System.out.print("Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        customerId = customerMgmt.registerCustomer(name, email, password);
                        System.out.println("Customer registered with ID: " + customerId);
                        break;

                    case "2":
                        if (customerId == null) {
                            System.out.println("\n[!] Register a customer first.");
                            break;
                        }
                        System.out.println("\n[Create Order]");
                        System.out.print("Enter description of the product: ");
                        String description = scanner.nextLine();
                        orderId = orderMgmt.createOrder(customerId, description);
                        System.out.println("Order created with ID: " + orderId);
                        break;

                    case "3":
                        if (orderId == null) {
                            System.out.println("\n[!] Create an order first.");
                            break;
                        }
                        System.out.println("\n[Ship Order]");
                        shipmentMgmt.shipOrder(orderId);
                        System.out.println("Order " + orderId + " shipped.");
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                        break;
                }
            }
            System.out.println("\n[✓] Session closed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InitialContext getContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        return new InitialContext(props);
    }
}
