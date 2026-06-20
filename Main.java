import javax.swing.*;

/**
 * The entry point of the Java Cafe Management System application.
 * Initializes and wires together the Order, Inventory, and Sales Report dashboards
 * inside a single tabbed main window.
 */
public class Main {
    /**
     * Main method to launch the Swing application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            // Creates the single main window of the system
            JFrame mainFrame = new JFrame("Java Cafe - Management System");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1000, 800);

            // Creates the tabbed pane component
            JTabbedPane tabbedPane = new JTabbedPane();

            // Creates the order screen and its controller
            OrderGUI orderScreen = new OrderGUI();
            OrderLogic logic = new OrderLogic(orderScreen);
            orderScreen.setController(logic);

            // Creates the inventory screen and its controller
            InventoryGUI inventoryScreen = new InventoryGUI();
            InventoryLogic invLogic = new InventoryLogic(inventoryScreen);
            inventoryScreen.setController(invLogic);
            
            // Connects the inventory logic to the order logic (for stock checks)
            logic.setInventoryLogic(invLogic);
            
            // Creates the sales report screen and links it
            SalesReportGUI reportScreen = new SalesReportGUI();
            logic.setReportScreen(reportScreen);

            // Binds the order interface to the inventory data. 
            invLogic.bindOrderSystem(orderScreen, logic);

            // Adds each panel as a different tab
            tabbedPane.addTab("Orders", orderScreen);
            tabbedPane.addTab("Inventory", inventoryScreen);
            tabbedPane.addTab("Sales Reports", reportScreen);

            // Adds the tabbed panel to the main window
            mainFrame.add(tabbedPane);
            
            // Centers and displays the window
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }
}