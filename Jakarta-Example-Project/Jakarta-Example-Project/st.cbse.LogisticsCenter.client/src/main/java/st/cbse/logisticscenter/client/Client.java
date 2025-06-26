package st.cbse.logisticscenter.client;

import javax.naming.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import st.cbse.crm.dto.OptionDTO;
import st.cbse.crm.dto.OrderDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.crm.dto.ShipmentItemDTO;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;
import st.cbse.crm.orderComponent.data.OrderStatus;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.crm.shipmentComponent.interfaces.IShipmentMgmt;

public class Client {

	/* --- EJB references -------------------------------------------------- */
	private static ICustomerMgmt customerMgmt;
	private static IManagerMgmt  managerMgmt;
	private static IOrderMgmt    orderMgmt;
	private static IShipmentMgmt shipmentMgmt;

	/* --- Session state --------------------------------------------------- */
	private static UUID loggedCustomer;
	private static String loggedManager;

	/* --- Cache pour la sélection par numéro ------------------------------ */
	private static Map<Integer, OrderDTO> orderCache = new HashMap<>();
	private static Map<Integer, PrintRequestDTO> requestCache = new HashMap<>();
	private static Map<Integer, ShipmentItemDTO> storageCache = new HashMap<>();

	private static final Scanner in = new Scanner(System.in);

	/* --------------------------------------------------------------------- */
	public static void main(String[] args) {
		try {
			InitialContext ctx = getContext();

			customerMgmt = (ICustomerMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/CustomerBean!st.cbse.crm.customerComponent.interfaces.ICustomerMgmt");
			managerMgmt  = (IManagerMgmt)  ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/ManagerBean!st.cbse.crm.managerComponent.interfaces.IManagerMgmt");
			orderMgmt    = (IOrderMgmt)    ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/OrderBean!st.cbse.crm.orderComponent.interfaces.IOrderMgmt");
			shipmentMgmt = (IShipmentMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.server/ShipmentBean!st.cbse.crm.shipmentComponent.interfaces.IShipmentMgmt");

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
		try {
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
		} catch(Exception e) {
			System.out.println("error : " + e.getMessage());
			return true;
		}
	}

	private static boolean customerMenu() throws Exception {
		try {
			System.out.println("\nCustomer menu");
			System.out.println("1  View order history");
			System.out.println("2  Create new order");
			System.out.println("3  Pay order");
			System.out.println("4  Logout");
			System.out.println("0  Exit");
			System.out.print("> ");
			switch (in.nextLine()) {
			case "1": viewOrderHistory(loggedCustomer);          return true;
			case "2": createNewOrder(loggedCustomer);            return true;
			case "3": payWithSelection(loggedCustomer);          return true;
			case "4": loggedCustomer = null; clearCache();       return true;
			case "0": return false;
			default:  System.out.println("Invalid choice."); return true;
			}
		} catch(Exception e) {
			System.out.println("error : " + e.getMessage());
			return true;
		}
	}

	private static boolean managerMenu() throws Exception {
		try {
			System.out.println("\nManager menu");
			System.out.println("1  List all orders");
			System.out.println("2  Add note to request");
			System.out.println("3  Mark request finished");
			System.out.println("4  Ship order");
			System.out.println("5  View finished items in storage");
			System.out.println("6  Logout");
			System.out.println("7  Send Order to the production");
			System.out.println("0  Exit");
			System.out.print("> ");
			switch (in.nextLine()) {
			case "1": listOrdersWithNumbers();                   return true;
			case "2": addNoteWithSelection();                    return true;
			case "3": markFinishedWithSelection();               return true;
			case "4": shipOrderWithSelection();                  return true;
			case "5": viewStorageWithNumbers();                  return true;
			case "6": loggedManager = null; clearCache();        return true;
			case "7": sendPrintToProdWithSelection();            return true;
			case "0": return false;
			default:  System.out.println("Invalid choice."); return true;
			}
		} catch(Exception e) {
			System.out.println("error " + e.getMessage());
			return true;
		}
	}

	/* ===================================================================== */
	/*  HELPER METHODS                                                       */
	/* ===================================================================== */

	private static void clearCache() {
		orderCache.clear();
		requestCache.clear();
		storageCache.clear();
	}

	private static OrderDTO selectOrder(String prompt) {
		if (orderCache.isEmpty()) {
			System.out.println("No orders available.");
			return null;
		}

		System.out.println("\n" + prompt);
		System.out.print("Select order number (0 to cancel): ");

		try {
			int choice = Integer.parseInt(in.nextLine());
			if (choice == 0) return null;

			OrderDTO selected = orderCache.get(choice);
			if (selected == null) {
				System.out.println("Invalid selection.");
			}
			return selected;
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
			return null;
		}
	}

	private static PrintRequestDTO selectRequest(String prompt) {
		if (requestCache.isEmpty()) {
			System.out.println("No requests available.");
			return null;
		}

		System.out.println("\n" + prompt);
		System.out.print("Select request number (0 to cancel): ");

		try {
			int choice = Integer.parseInt(in.nextLine());
			if (choice == 0) return null;

			PrintRequestDTO selected = requestCache.get(choice);
			if (selected == null) {
				System.out.println("Invalid selection.");
			}
			return selected;
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
			return null;
		}
	}

	/* ===================================================================== */
	/*  AUTH / REGISTRATION                                                  */
	/* ===================================================================== */

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
		try {
			loggedCustomer = customerMgmt.loginCustomer(email, pw);
			System.out.println("logged as a Customer !");
		} catch(Exception ex) {
			System.out.println("bad credentials");
			loggedCustomer = null;
		}
	}

	private static void loginManager() throws Exception {
		System.out.println("\n[Manager login]");
		System.out.print("E-mail   : "); String email = in.nextLine();
		System.out.print("Password : "); String pw    = in.nextLine();
		try {
			loggedManager = managerMgmt.loginManager(email, pw);
			System.out.println("Logged in as manager.");
		} catch(Exception ex) {
			System.out.println("bad credentials");
			loggedManager = null;
		}
	}

	/* ---------------- Order history ---------------- */
	private static void viewOrderHistory(UUID customerId) throws Exception {
		List<OrderDTO> orders = orderMgmt.getOrdersByCustomer(customerId);

		if (orders.isEmpty()) {
			System.out.println("No orders yet.");
			return;
		}

		orderCache.clear();
		requestCache.clear();
		int orderNum = 1;
		int globalRequestNum = 1;

		for (OrderDTO o : orders) {
			orderCache.put(orderNum, o);

			System.out.printf("\n[%d] Order Status: %s  Total: €%s%n",
					orderNum++, o.getStatus(), o.getTotal());

			for (PrintRequestDTO pr : o.getPrintingRequests()) {
				requestCache.put(globalRequestNum, pr);

				System.out.printf("    [Req %d] STL=%s%n", 
						globalRequestNum++, pr.getStlPath());

				if (pr.getNote() != null && !pr.getNote().isBlank())
					System.out.println("          Note: " + pr.getNote());

				for (OptionDTO op : pr.getOptions()) {
					System.out.printf("          %-15s  €%s%n", 
							op.getType(), op.getPrice());
				}
			}
		}
	}

	/* ------------------- Création d'une commande --------------------- */
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
					break;
				}
				case "2" : {
					System.out.print("Granularity: ");
					String g = in.nextLine();
					orderMgmt.addSmoothingOption(requestId, g);
					break;
				}
				case "3" : {
					System.out.print("Text  : ");
					String text = in.nextLine();
					System.out.print("Font  : ");
					String font = in.nextLine();
					System.out.print("Image path (enter to skip): ");
					String img = in.nextLine();
					orderMgmt.addEngravingOption(requestId, text, font, img);
					break;
				}
				default : moreOpt = false;
				}
			}
			System.out.print("Add another print-request? (y/N) ");
			moreReq = in.nextLine().equalsIgnoreCase("y");
		}
		orderMgmt.finalizeOrder(orderId);
		System.out.println("[✓] Order submitted!");
	}

	/* ------------------------- Paiement ------------------------------ */
	private static void payWithSelection(UUID customerId) throws Exception {
		try {
			// D'abord afficher les commandes
			viewOrderHistory(customerId);

			// Filtrer uniquement les commandes non payées
			Map<Integer, OrderDTO> unpaidOrders = new HashMap<>();
			int num = 1;
			for (Map.Entry<Integer, OrderDTO> entry : orderCache.entrySet()) {
				OrderDTO order = entry.getValue();
				if ("PENDING".equals(order.getStatus()) || "CREATED".equals(order.getStatus())) {
					unpaidOrders.put(num++, order);
				}
			}

			if (unpaidOrders.isEmpty()) {
				System.out.println("\nNo unpaid orders.");
				return;
			}

			// Remplacer le cache temporairement
			orderCache = unpaidOrders;
			OrderDTO selectedOrder = selectOrder("Select order to pay:");

			if (selectedOrder != null) {
				System.out.print("Transaction reference: ");
				String ref = in.nextLine();
				orderMgmt.pay(selectedOrder.getId(), ref);
				System.out.println("[✓] Payment booked.");
			}
		} catch(Exception e) {            System.out.println("Payment failed: " + e.getMessage());
		}
	}

	/* ===================================================================== */
	/*  MANAGER WORKFLOWS                                                    */
	/* ===================================================================== */

	private static void listOrdersWithNumbers() throws Exception {
		List<OrderDTO> orders = managerMgmt.listAllOrders();
		if (orders.isEmpty()) { 
			System.out.println("No orders."); 
			return; 
		}

		orderCache.clear();
		int num = 1;

		System.out.println("\n=== All Orders ===");
		for (OrderDTO o : orders) {
			orderCache.put(num, o);
			System.out.printf("[%d] %-11s  Customer=%s  Total=€%s%n",
					num++, o.getStatus(), o.getCustomer(), o.getTotal());
		}
	}

	private static void addNoteWithSelection() throws Exception {
		listOrdersWithNumbers();
		OrderDTO selectedOrder = selectOrder("Select order containing the request:");
		if (selectedOrder == null) return;

		// Afficher les requests de cette commande
		requestCache.clear();
		int num = 1;
		System.out.println("\n=== Printing Requests ===");
		for (PrintRequestDTO pr : selectedOrder.getPrintingRequests()) {
			requestCache.put(num, pr);
			System.out.printf("[%d] STL=%s%n", num++, pr.getStlPath());
			if (pr.getNote() != null && !pr.getNote().isBlank()) {
				System.out.println("    Current note: " + pr.getNote());
			}
		}

		PrintRequestDTO selectedRequest = selectRequest("Select request to add note:");
		if (selectedRequest != null) {
			System.out.print("Note: ");
			String note = in.nextLine();
			managerMgmt.addNoteToRequest(selectedRequest.getId(), note);
			System.out.println("Note added.");
		}
	}

	private static void markFinishedWithSelection() throws Exception {
		listOrdersWithNumbers();
		OrderDTO selectedOrder = selectOrder("Select order containing the request:");
		if (selectedOrder == null) return;

		// Afficher les requests de cette commande
		requestCache.clear();
		int num = 1;
		System.out.println("\n=== Printing Requests ===");
		for (PrintRequestDTO pr : selectedOrder.getPrintingRequests()) {
			requestCache.put(num, pr);
			System.out.printf("[%d] STL=%s%n", num++, pr.getStlPath());
		}

		PrintRequestDTO selectedRequest = selectRequest("Select request to mark as finished:");
		if (selectedRequest != null) {
			managerMgmt.markRequestFinished(selectedRequest.getId());
			System.out.println("Request marked finished.");
		}
	}

	private static void sendPrintToProdWithSelection() throws Exception {
		listOrdersWithNumbers();
		OrderDTO selectedOrder = selectOrder("Select order to send to production:");

		if (selectedOrder != null && selectedOrder.getStatus() == OrderStatus.COMPLETED.toString()) {
			managerMgmt.sendPrintToProd(selectedOrder.getId());
			System.out.println("Order sent to production.");
		}else{
			System.out.println("order not completed");
		}
	}

	private static void shipOrderWithSelection() throws Exception {
		// TODO: Implémenter avec ShipmentDTO si nécessaire
		System.out.println("Ship order - Not implemented yet");
	}

	private static void viewStorageWithNumbers() throws Exception {
		List<ShipmentItemDTO> items = shipmentMgmt.itemsInStorage();
		if (items.isEmpty()) { 
			System.out.println("Storage empty."); 
			return; 
		}

		storageCache.clear();
		int num = 1;

		System.out.println("\n=== Items in Storage ===");
		for (ShipmentItemDTO item : items) {
			storageCache.put(num, item);
			System.out.printf("[%d] Request=%s  Order=%s%n",
					num++, item.getPrintRequestId(), item.getOrderId());
		}
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