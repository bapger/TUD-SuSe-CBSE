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
import st.cbse.productionFacility.process.data.enums.ProcessStatus;
import st.cbse.productionFacility.process.dto.ProcessDTO;
import st.cbse.crm.dto.PrintRequestDTO;
import st.cbse.crm.dto.ShipmentItemDTO;
import st.cbse.crm.customerComponent.interfaces.ICustomerMgmt;
import st.cbse.crm.managerComponent.interfaces.IManagerMgmt;
import st.cbse.crm.orderComponent.data.OrderStatus;
import st.cbse.productionFacility.process.data.enums.ProcessStatus;
import st.cbse.crm.orderComponent.interfaces.IOrderMgmt;
import st.cbse.productionFacility.process.interfaces.IProcessMgmt;
import st.cbse.shipment.interfaces.IShipmentMgmt;
import st.cbse.productionFacility.productionManagerComponent.interfaces.IProductionManagerMgmt;
import st.cbse.productionFacility.storage.dto.FinishedProductsDto;
import st.cbse.productionFacility.storage.interfaces.IStorageMgmt;

public class Client {

	// EJB references
	private static ICustomerMgmt customerMgmt;
	private static IManagerMgmt managerMgmt;
	private static IProductionManagerMgmt productionManagerMgmt;
	private static IOrderMgmt orderMgmt;
	private static IProcessMgmt processMgmt;
	private static IShipmentMgmt shipmentMgmt;
	private static IStorageMgmt storageMgmt;

	// Session state
	private static UUID loggedCustomer;
	private static String loggedManager;
	private static String loggedProductionManager;

	// Cache pour la sélection par numéro
	private static Map<Integer, OrderDTO> orderCache = new HashMap<>();
	private static Map<Integer, PrintRequestDTO> requestCache = new HashMap<>();
	private static Map<Integer, FinishedProductsDto> storageCache = new HashMap<>();

	private static final Scanner in = new Scanner(System.in);

	public static void main(String[] args) {
		try {
			InitialContext ctx = getContext();

			customerMgmt = (ICustomerMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/CustomerBean!st.cbse.crm.customerComponent.interfaces.ICustomerMgmt");
			managerMgmt = (IManagerMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/ManagerBean!st.cbse.crm.managerComponent.interfaces.IManagerMgmt");
			productionManagerMgmt = (IProductionManagerMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/ProductionManagerBean!st.cbse.productionFacility.productionManagerComponent.interfaces.IProductionManagerMgmt");
			orderMgmt = (IOrderMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/OrderBean!st.cbse.crm.orderComponent.interfaces.IOrderMgmt");
			processMgmt = (IProcessMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/ProcessBean!st.cbse.productionFacility.process.interfaces.IProcessMgmt");
			shipmentMgmt = (IShipmentMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/ShipmentBean!st.cbse.shipment.interfaces.IShipmentMgmt");
			storageMgmt = (IStorageMgmt) ctx.lookup(
					"ejb:/st.cbse.LogisticsCenter.CRM.server/StorageBean!st.cbse.productionFacility.storage.interfaces.IStorageMgmt");
			System.out.println("=== Logistics System Client ===");

			// very unaesthetic
			boolean running = true;
			while (running) {
				if (loggedCustomer == null && loggedManager == null && loggedProductionManager == null) {
					running = mainMenu();
				} else if (loggedCustomer != null) {
					running = customerMenu();
				} else if (loggedManager != null) {
					running = managerMenu();
				} else if (loggedProductionManager != null) {
					running = productionManagerMenu();
				}
			}
			System.out.println("[✓] Session closed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * =====================================================================
	 * MENUS
	 * =====================================================================
	 */

	private static boolean mainMenu() throws Exception {
		try {
			System.out.println("\nMain menu");
			System.out.println("1  Register as customer");
			System.out.println("2  Login as customer");
			System.out.println("3  Login as manager");
			System.out.println("4  Login as production manager");
			System.out.println("0  Exit");
			System.out.print("> ");
			switch (in.nextLine()) {
				case "1":
					registerCustomer();
					return true;
				case "2":
					loginCustomer();
					return true;
				case "3":
					loginManager();
					return true;
				case "4":
					loginProductionManager();
					return true;
				case "0":
					return false;
				default:
					System.out.println("Invalid choice.");
					return true;
			}
		} catch (Exception e) {
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
				case "1":
					viewOrderHistory(loggedCustomer);
					return true;
				case "2":
					createNewOrder(loggedCustomer);
					return true;
				case "3":
					payWithSelection(loggedCustomer);
					return true;
				case "4":
					loggedCustomer = null;
					clearCache();
					return true;
				case "0":
					return false;
				default:
					System.out.println("Invalid choice.");
					return true;
			}
		} catch (Exception e) {
			System.out.println("error : " + e.getMessage());
			return true;
		}
	}

	private static boolean managerMenu() throws Exception {
		try {
			System.out.println("\nManager menu");
			System.out.println("1  List all orders");
			System.out.println("2  Add note to request");
			System.out.println("4  Ship order");
			System.out.println("5  View finished items in storage");
			System.out.println("6  Send Order to the production");
			System.out.println("7  Logout");
			System.out.println("0  Exit");
			System.out.print("> ");
			switch (in.nextLine()) {
				case "1":
					listOrdersWithNumbers();
					return true;
				case "2":
					addNoteWithSelection();
					return true;
				case "3":
					markFinishedWithSelection();
					return true;
				case "4":
					shipOrderWithSelection();
					return true;
				case "5":
					viewStorageWithNumbers();
					return true;
				case "7":
					loggedManager = null;
					clearCache();
					return true;
				case "6":
					sendPrintToProdWithSelection();
					return true;
				case "0":
					return false;
				default:
					System.out.println("Invalid choice.");
					return true;
			}
		} catch (Exception e) {
			System.out.println("error " + e.getMessage());
			return true;
		}
	}

	private static boolean productionManagerMenu() throws Exception {
		try {
			System.out.println("\nProduction manager menu");
			System.out.println("1  List processes");
			System.out.println("2  Get processes by status");
			System.out.println("3  Pause a process");
			System.out.println("4  Resume a process");
			System.out.println("5  Cancel a process");
			System.out.println("6  Logout");
			System.out.println("0  Exit");
			System.out.print("> ");

			List<ProcessDTO> processes;
			ProcessDTO chosenProcess;

			switch (in.nextLine()) {
				case "1":
					listProcesses();
					return true;
				case "2":
					ProcessStatus[] status = ProcessStatus.values();
					for (int i = 1; i < status.length + 1; i++) {
						System.out.println(i + " : " + status[i - 1]);
					}
					int choiceIndex = Integer.parseInt(in.nextLine());
					ProcessStatus chosenStatus = status[choiceIndex - 1];
					System.out.println("You chose : " + chosenStatus.name());
					getProcessesByStatus(chosenStatus);
					return true;
				case "3":
					processes = processMgmt.getProcessesByStatus("IN_PROGRESS");

					for (int i = 1; i < processes.size() + 1; i++) {
						System.out.println(i + " : " + processes.get(i - 1).getId());
					}
					System.out.println("Choose process to pause");
					chosenProcess = processes.get(Integer.parseInt(in.nextLine()) - 1);

					System.out.println("You have chosen : " + chosenProcess.getId());
					pauseProcess(chosenProcess);
					return true;
				case "4":
					processes = processMgmt.getProcessesByStatus("PAUSED");

					for (int i = 1; i < processes.size() + 1; i++) {
						System.out.println(i + " : " + processes.get(i - 1).getId());
					}
					System.out.println("Choose paused process to resume");
					chosenProcess = processes.get(Integer.parseInt(in.nextLine()) - 1);

					System.out.println("You have chosen : " + chosenProcess.getId());
					resumeProcess(chosenProcess);
					return true;
				case "5":
					processes = productionManagerMgmt.getAllProcesses();

					for (int i = 1; i < processes.size() + 1; i++) {
						System.out.println(i + " : " + processes.get(i - 1).getId());
					}
					System.out.println("Choose process to stop");
					chosenProcess = processes.get(Integer.parseInt(in.nextLine()) - 1);

					System.out.println("You have chosen : " + chosenProcess.getId());
					cancelProcess(chosenProcess);
					return true;
				case "6":
					loggedProductionManager = null;
					clearCache();
					return true;
				case "0":
					return false;
				default:
					System.out.println("Invalid choice.");
					return true;
			}
		} catch (Exception e) {
			System.out.println("error : " + e.getMessage());
			return true;
		}
	}

	/*
	 * =====================================================================
	 * HELPER METHODS
	 * =====================================================================
	 */

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
			if (choice == 0)
				return null;

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
			if (choice == 0)
				return null;

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

	/*
	 * =====================================================================
	 * AUTH / REGISTRATION
	 * =====================================================================
	 */

	private static void registerCustomer() throws Exception {
		System.out.println("\n[Register customer]");
		System.out.print("Full name : ");
		String name = in.nextLine();
		System.out.print("E-mail    : ");
		String email = in.nextLine();
		System.out.print("Password  : ");
		String pw = in.nextLine();
		loggedCustomer = customerMgmt.registerCustomer(name, email, pw);
		System.out.println("Registered. Your id: " + loggedCustomer);
	}

	private static void loginCustomer() throws Exception {
		System.out.println("\n[Customer login]");
		System.out.print("E-mail   : ");
		String email = in.nextLine();
		System.out.print("Password : ");
		String pw = in.nextLine();
		try {
			loggedCustomer = customerMgmt.loginCustomer(email, pw);
			System.out.println("logged as a Customer !");
		} catch (Exception ex) {
			System.out.println("bad credentials");
			loggedCustomer = null;
		}
	}

	private static void loginManager() throws Exception {
		System.out.println("\n[Manager login]");
		System.out.print("E-mail   : ");
		String email = in.nextLine();
		System.out.print("Password : ");
		String pw = in.nextLine();
		try {
			loggedManager = managerMgmt.loginManager(email, pw);
			System.out.println("Logged in as manager.");
		} catch (Exception ex) {
			System.out.println("bad credentials");
			loggedManager = null;
		}
	}

	private static void loginProductionManager() throws Exception {
		System.out.println("\n[Production manager login]");
		System.out.print("E-mail   : ");
		String email = in.nextLine();
		System.out.print("Password : ");
		String pw = in.nextLine();
		try {
			loggedProductionManager = productionManagerMgmt.loginProductionManager(email, pw);
			System.out.println("Logged in as production manager.");
		} catch (Exception ex) {
			System.out.println("bad credentials");
			loggedManager = null;
		}
	}

	// Order history
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

	// Order creation
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
					case "1": {
						System.out.print("Colour: ");
						String colour = in.nextLine();
						System.out.print("Layer count: ");
						int layers = Integer.parseInt(in.nextLine());
						orderMgmt.addPaintJobOption(requestId, colour, layers);
						break;
					}
					case "2": {
						System.out.print("Granularity: ");
						String g = in.nextLine();
						orderMgmt.addSmoothingOption(requestId, g);
						break;
					}
					case "3": {
						System.out.print("Text  : ");
						String text = in.nextLine();
						System.out.print("Font  : ");
						String font = in.nextLine();
						System.out.print("Image path (enter to skip): ");
						String img = in.nextLine();
						orderMgmt.addEngravingOption(requestId, text, font, img);
						break;
					}
					default:
						moreOpt = false;
				}
			}
			System.out.print("Add another print-request? (y/N) ");
			moreReq = in.nextLine().equalsIgnoreCase("y");
		}
		orderMgmt.finalizeOrder(orderId);
		System.out.println("[✓] Order submitted!");
	}

	//Payement
	private static void payWithSelection(UUID customerId) throws Exception {
	    try {
	        
	        List<OrderDTO> orders = orderMgmt.getOrdersByCustomer(customerId);
	        
	        if (orders.isEmpty()) {
	            System.out.println("No orders found.");
	            return;
	        }
	        
	        Map<Integer, OrderDTO> payableItems = new HashMap<>();
	        int num = 1;
	        
	        System.out.println("\n=== Payment Options ===");
	        
	        System.out.println("\n-- Unpaid Invoices (Post-shipping) --");
	        boolean hasUnpaidInvoices = false;
	        for (OrderDTO order : orders) {
	            if (OrderStatus.SHIPPED.toString().equals(order.getStatus())) {
	                try {
	                    if (orderMgmt.hasUnpaidInvoice(order.getId())) {
	                        payableItems.put(num, order);
	                        System.out.printf("[%d] Invoice for Order %s - €%s%n",
	                                num++, order.getId().toString().substring(0, 8), 
	                                order.getTotal());
	                        System.out.println("      Items:");
	                        for (PrintRequestDTO pr : order.getPrintingRequests()) {
	                            System.out.printf("        - %s%n", pr.getStlPath());
	                        }
	                        hasUnpaidInvoices = true;
	                    }
	                } catch (Exception e) {
	                }
	            }
	        }
	        if (!hasUnpaidInvoices) {
	            System.out.println("  None");
	        }
	        
	        if (payableItems.isEmpty()) {
	            System.out.println("\n[✓] All orders and invoices are paid!");
	            return;
	        }
	        
	        orderCache = payableItems;
	        OrderDTO selectedOrder = selectOrder("Select item to pay:");
	        
	        if (selectedOrder != null) {
	            System.out.print("Transaction reference: ");
	            String ref = in.nextLine();
	            
	            if (OrderStatus.SHIPPED.toString().equals(selectedOrder.getStatus())) {
	                System.out.println("\n[...] Processing invoice payment...");
	                orderMgmt.payInvoice(selectedOrder.getId(), ref);
	                System.out.println("[✓] Invoice paid successfully!");
	                System.out.println("    Thank you for your payment.");
	            } else {
	                System.out.println("\n[...] Processing order payment...");
	                orderMgmt.pay(selectedOrder.getId(), ref);
	                System.out.println("[✓] Order payment booked.");
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("Payment failed: " + e.getMessage());
	    }
	}

	/* ===================================================================== 
	* MANAGER WORKFLOWS
	* ===================================================================== */

	private static void listOrdersWithNumbers() throws Exception {
	    List<OrderDTO> orders = managerMgmt.listAllOrders();
	    if (orders.isEmpty()) {
	        System.out.println("No orders.");
	        return;
	    }

	    orderCache.clear();
	    requestCache.clear();
	    int orderNum = 1;
	    int globalRequestNum = 1;

	    System.out.println("\n=== All Orders ===");
	    for (OrderDTO o : orders) {
	        orderCache.put(orderNum, o);
	        
	        System.out.printf("%n[%d] Order ID: %s%n", orderNum, o.getId());
	        System.out.printf("    Status: %-12s | Customer: %s | Total: €%s%n",
	                o.getStatus(), o.getCustomer(), o.getTotal());
	        
	        if (o.getPrintingRequests() != null && !o.getPrintingRequests().isEmpty()) {
	            System.out.println("    Printing Requests:");
	            
	            for (PrintRequestDTO pr : o.getPrintingRequests()) {
	                requestCache.put(globalRequestNum, pr);
	                
	                System.out.printf("      [Req %d] STL: %s%n", 
	                        globalRequestNum++, pr.getStlPath());
	                
	                if (pr.getNote() != null && !pr.getNote().isBlank()) {
	                    System.out.printf("              Note: %s%n", pr.getNote());
	                }
	                
	                if (pr.getOptions() != null && !pr.getOptions().isEmpty()) {
	                    System.out.println("              Options:");
	                    for (OptionDTO op : pr.getOptions()) {
	                        System.out.printf("                - %-15s €%s%n", 
	                                op.getType(), op.getPrice());
	                    }
	                }
	            }
	        } else {
	            System.out.println("    No printing requests");
	        }
	        
	        orderNum++;
	    }
	    
	    System.out.printf("%n=== Total Orders: %d ===%n", orders.size());
	}

	private static void addNoteWithSelection() throws Exception {
		listOrdersWithNumbers();
		OrderDTO selectedOrder = selectOrder("Select order containing the request:");
		if (selectedOrder == null)
			return;

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
		if (selectedOrder == null)
			return;

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

		if (selectedOrder != null) {
			managerMgmt.sendPrintToProd(selectedOrder.getId());
			System.out.println("Order sent to production.");
		} else {
			System.out.println("order not completed");
		}
	}

	private static void shipOrderWithSelection() throws Exception {
	    listOrdersWithNumbers();
	    
	    Map<Integer, OrderDTO> finishedOrders = new HashMap<>();
	    int num = 1;
	    
	    System.out.println("\n=== Orders Ready to Ship ===");
	    boolean foundFinished = false;
	    
	    for (Map.Entry<Integer, OrderDTO> entry : orderCache.entrySet()) {
	        OrderDTO order = entry.getValue();
	        if (OrderStatus.FINISHED.toString().equals(order.getStatus())) {
	            finishedOrders.put(num, order);
	            System.out.printf("[%d] Order ID: %s | Customer: %s | Total: €%s%n",
	                    num++, order.getId(), order.getCustomer(), order.getTotal());
	            foundFinished = true;
	        }
	    }
	    
	    if (!foundFinished) {
	        System.out.println("\n[!] No orders are ready to ship (must be FINISHED status).");
	        return;
	    }
	    
	    orderCache = finishedOrders;
	    OrderDTO selectedOrder = selectOrder("Select order to ship:");
	    
	    if (selectedOrder != null) {
	        try {
	            System.out.println("\n[...] Processing shipment...");
	            
	            shipmentMgmt.shipOrder(selectedOrder.getId());
	            orderMgmt.createInvoiceForShippedOrder(selectedOrder.getId());
	            
	            System.out.println("\n[✓] Order shipped successfully!");
	            System.out.println("[✓] Invoice created:");
	            System.out.printf("    - Invoice Amount: €%s%n", selectedOrder.getTotal());
	            System.out.println("    - Status: Awaiting Payment");
	            System.out.println("    - Customer has been notified");
	            
	        } catch (Exception e) {
	            System.out.println("[✗] Failed to ship order: " + e.getMessage());
	        }
	    }
	}

	private static void viewStorageWithNumbers() throws Exception {
		List<FinishedProductsDto> items = storageMgmt.getAllFinishedProducts();
		if (items.isEmpty()) {
			System.out.println("Storage empty.");
			return;
		}

		storageCache.clear();
		int num = 1;

		System.out.println("\n=== Items in Storage ===");
		for (FinishedProductsDto item : items) {
			storageCache.put(num, item);
			System.out.printf("[%d] Request=%s%n",
					num++, item.getPrintRequestId());
		}
	}

	private static void listProcesses() throws Exception {
		List<ProcessDTO> processes = productionManagerMgmt.getAllProcesses();
		if (processes.isEmpty()) {
			System.out.println("No processes.");
			return;
		}

		System.out.println("\n=== All Processes ===");
		for (ProcessDTO p : processes) {
			System.out.printf("ID = "+p.getId()+" Status = %s  Progress= %s\n",
					p.getStatus(), p.getProgressPercentage());
		}
	}

	private static List<ProcessDTO> getProcessesByStatus(ProcessStatus status) {
		return processMgmt.getProcessesByStatus(status.name());
	}

	private static void pauseProcess(ProcessDTO process) {
		processMgmt.pauseProcess(process.getId());
	}

	private static void resumeProcess(ProcessDTO process) {
		processMgmt.resumeProcess(process.getId());
	}

	private static void cancelProcess(ProcessDTO process) {
		processMgmt.cancelProcess(process.getId());
	}
	/* =====================================================================
	* JNDI context helper
	* ===================================================================== */

	private static InitialContext getContext() throws NamingException {
		Properties p = new Properties();
		p.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.wildfly.naming.client.WildFlyInitialContextFactory");
		p.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
		return new InitialContext(p);
	}
}