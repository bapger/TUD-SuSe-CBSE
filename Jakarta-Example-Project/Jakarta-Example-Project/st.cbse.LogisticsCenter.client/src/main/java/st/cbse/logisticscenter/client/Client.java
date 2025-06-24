package st.cbse.logisticscenter.client;

import javax.naming.*;
import java.math.BigDecimal;
import java.util.*;

import st.cbse.crm.dto.OptionDTO;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.crm.dto.ShipmentItemDTO;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.shipment.interfaces.IShipmentMgmt;

public class Client {

    /* --- EJB references -------------------------------------------------- */
    private static ICustomerMgmt customerMgmt;
    private static IManagerMgmt  managerMgmt;
    private static IOrderMgmt    orderMgmt;
    private static IShipmentMgmt shipmentMgmt;

    /* --- Session state --------------------------------------------------- */
    private static UUID loggedCustomer;
    private static UUID loggedManager;

    private static final Scanner in = new Scanner(System.in);

    /* --------------------------------------------------------------------- */
    public static void main(String[] args) {
        try {
            InitialContext ctx = getContext();

            customerMgmt = (ICustomerMgmt) ctx.lookup(
                "ejb:/st.cbse.LogisticsCenter.CRM.server/CustomerBean!st.cbse.crm.interfaces.ICustomerMgmt");
            managerMgmt  = (IManagerMgmt)  ctx.lookup(
                "ejb:/st.cbse.LogisticsCenter.CRM.server/CustomerBean!st.cbse.crm.interfaces.IManagerMgmt");
            orderMgmt    = (IOrderMgmt)    ctx.lookup(
                "ejb:/st.cbse.LogisticsCenter.CRM.server/OrderBean!st.cbse.crm.interfaces.IOrderMgmt");
            shipmentMgmt = (IShipmentMgmt) ctx.lookup(
                "ejb:/st.cbse.LogisticsCenter.server/ShipmentBean!st.cbse.crm.shipment.interfaces.IShipmentMgmt");

            System.out.println("=== Logistics System Client ===");

            boolean running = true;
            while (running) {
                if (loggedCustomer == null && loggedManager == null) {
                    running = mainMenu();
                } else if (loggedCustomer != null) {
                    running = customerMenu();
                } else {
                    running = managerMenu();
                }
            }
            System.out.println("[✓] Session closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================================================================== */
    /*  MENUS                                                                */
    /* ===================================================================== */

    private static boolean mainMenu() throws Exception {
        System.out.println("\nMain menu");
        System.out.println("1  Register as customer");
        System.out.println("2  Login as customer");
        System.out.println("3  Login as manager");
        System.out.println("0  Exit");
        System.out.print("> ");
        switch (in.nextLine()) {
            case "1": registerCustomer();          return true;
            case "2": loginCustomer();             return true;
            case "3": loginManager();              return true;
            case "0": return false;
            default:  System.out.println("Invalid choice."); return true;
        }
    }

    private static boolean customerMenu() throws Exception {
        System.out.println("\nCustomer menu");
        System.out.println("1  View order history");
        System.out.println("2  Create new order");
        System.out.println("3  Pay order");
        System.out.println("4  Logout");
        System.out.println("0  Exit");
        System.out.print("> ");
        switch (in.nextLine()) {
            case "1": viewOrderHistory(loggedCustomer);     return true;
            case "2": createNewOrder(loggedCustomer);       return true;
            case "3": pay(loggedCustomer);                  return true;
            case "4": loggedCustomer = null;                return true;
            case "0": return false;
            default:  System.out.println("Invalid choice."); return true;
        }
    }

    private static boolean managerMenu() throws Exception {
        System.out.println("\nManager menu");
        System.out.println("1  List all orders");
        System.out.println("2  Add note to request");
        System.out.println("3  Mark request finished");
        System.out.println("4  Ship order");
        System.out.println("5  View finished items in storage");
        System.out.println("6  Logout");
        System.out.println("0  Exit");
        System.out.print("> ");
        switch (in.nextLine()) {
            case "1": listOrders();                    return true;
            case "2": addNote();                       return true;
            case "3": markFinished();                  return true;
            case "4": shipOrder();                     return true;
            case "5": viewStorage();                   return true;
            case "6": loggedManager = null;            return true;
            case "0": return false;
            default:  System.out.println("Invalid choice."); return true;
        }
    }

    /* ===================================================================== */
    /*  AUTH / REGISTRATION                                                  */
    /* ===================================================================== */

    private static void shipOrder() {
		// TODO Auto-generated method stub
		
	}

	private static void registerCustomer() throws Exception {
        System.out.println("\n[Register customer]");
        System.out.print("Full name : "); String name = in.nextLine();
        System.out.print("E-mail    : "); String email = in.nextLine();
        System.out.print("Password  : "); String pw = in.nextLine();
        loggedCustomer = customerMgmt.registerCustomer(name, email, pw);
        System.out.println("Registered. Your id: " + loggedCustomer);
    }
    private static void loginCustomer() throws Exception {
        System.out.println("\n[Customer login]");
        System.out.print("E-mail   : "); String email = in.nextLine();
        System.out.print("Password : "); String pw    = in.nextLine();
        loggedCustomer = customerMgmt.loginCustomer(email, pw);
        System.out.println("Welcome back!");
    }
    private static void loginManager() throws Exception {
        System.out.println("\n[Manager login]");
        System.out.print("E-mail   : "); String email = in.nextLine();
        System.out.print("Password : "); String pw    = in.nextLine();
        loggedManager = managerMgmt.loginManager(email, pw);
        System.out.println("Logged in as manager.");
    }

    /* ---------------- Order history ---------------- */
    private static void viewOrderHistory(UUID customerId) throws Exception {

        List<OrderDTO> orders = orderMgmt.getOrdersByCustomer(customerId);

        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }

        /* Simple pretty-print of the immutable DTO hierarchy */
        for (OrderDTO o : orders) {

            System.out.printf(
                "\nOrder %s  Status: %s  Total: €%s%n",
                o.getId(), o.getStatus(), o.getTotal());

            for (PrintRequestDTO pr : o.getPrintingRequests()) {
                System.out.println("  Request " + pr.getId()
                                   + "   STL=" + pr.getStlPath());

                if (pr.getNote() != null && !pr.getNote().isBlank())
                    System.out.println("    Note: " + pr.getNote());

                for (OptionDTO op : pr.getOptions()) {
                    System.out.printf("    %-15s  €%s%n",
                                      op.getType(), op.getPrice());
                }
            }
        }
    }

    /* ------------------- Création d’une commande --------------------- */
    private static void createNewOrder(UUID customerId) throws Exception {
        System.out.print("Base price (€): ");
        BigDecimal base = new BigDecimal(in.nextLine());
        UUID orderId = orderMgmt.createOrder(customerId, base);

        boolean moreReq = true;
        while (moreReq) {
            System.out.print("STL file path : ");
            String stl = in.nextLine();
            System.out.print("Note (Enter to skip): ");
            String note = in.nextLine();
            UUID requestId = orderMgmt.addPrintRequest(orderId, stl, note);

            boolean moreOpt = true;
            while (moreOpt) {
                System.out.println("Add option: 1-Paint 2-Smooth 3-Engrave 0-Done");
                switch (in.nextLine()) {
                    case "1" : {
                        System.out.print("Colour: ");
                        String colour = in.nextLine();
                        System.out.print("Layer count: ");
                        int layers = Integer.parseInt(in.nextLine());
                        orderMgmt.addPaintJobOption(requestId, colour, layers);
                    }
                    case "2" : {
                        System.out.print("Granularity: ");
                        String g = in.nextLine();
                        orderMgmt.addSmoothingOption(requestId, g);
                    }
                    case "3" : {
                        System.out.print("Text  : ");
                        String text = in.nextLine();
                        System.out.print("Font  : ");
                        String font = in.nextLine();
                        System.out.print("Image path (enter to skip): ");
                        String img = in.nextLine();
                        orderMgmt.addEngravingOption(requestId, text, font, img);
                    }
                    default : moreOpt = false;
                }
            }
            System.out.print("Add another print-request? (y/N) ");
            moreReq = in.nextLine().equalsIgnoreCase("y");
        }
        orderMgmt.finalizeOrder(orderId);
        System.out.println("[✓] Order submitted!  Id = " + orderId);
    }

    /* ------------------------- Paiement ------------------------------ */
    private static void pay(UUID customerId) throws Exception {
        System.out.print("Order id to pay: ");
        UUID orderId = UUID.fromString(in.nextLine());
        System.out.print("Transaction reference: ");
        String ref = in.nextLine();
        orderMgmt.pay(orderId, ref);
        System.out.println("[✓] Payment booked.");
    }
    /* ===================================================================== */
    /*  MANAGER WORKFLOWS                                                    */
    /* ===================================================================== */

    private static void listOrders() throws Exception {
        List<OrderDTO> orders = managerMgmt.listAllOrders();
        if (orders.isEmpty()) { System.out.println("No orders."); return; }
        orders.forEach(o -> System.out.printf(
            "%s  %-11s  Customer=%s  Total=€%s%n",
            o.getId(), o.getStatus(), o.getCustomerName(), o.getTotal()));
    }

    private static void addNote() throws Exception {
        System.out.print("Request id: ");
        UUID reqId = UUID.fromString(in.nextLine());
        System.out.print("Note: ");
        String note = in.nextLine();
        managerMgmt.addNoteToRequest(reqId, note);
        System.out.println("Note added.");
    }

    private static void markFinished() throws Exception {
        System.out.print("Request id: ");
        UUID reqId = UUID.fromString(in.nextLine());
        managerMgmt.markRequestFinished(reqId);
        System.out.println("Request marked finished.");
    }

    /*private static void shipOrder() throws Exception {
        System.out.print("Order id to ship: ");
        UUID orderId = UUID.fromString(in.nextLine());
        System.out.print("Customer address: ");
        String addr = in.nextLine();
        System.out.print("Tracking number: ");
        String track = in.nextLine();
        ShipmentDTO dto = new ShipmentDTO(addr, track);
        managerMgmt.shipOrder(orderId, dto);
        System.out.println("Order shipped.");
    }*/

    private static void viewStorage() throws Exception {
        List<ShipmentItemDTO> items = shipmentMgmt.itemsInStorage();
        if (items.isEmpty()) { System.out.println("Storage empty."); return; }
        items.forEach(i -> System.out.printf("%s  Order=%s%n",
                                             i.getPrintRequestId(),
                                             i.getOrderId()));
    }

    /* ===================================================================== */
    /*  JNDI context helper                                                  */
    /* ===================================================================== */

    private static InitialContext getContext() throws NamingException {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY,
              "org.wildfly.naming.client.WildFlyInitialContextFactory");
        p.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        return new InitialContext(p);
    }
}