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
        // Set the application name on macOS menu bar/dock
        System.setProperty("apple.awt.application.name", "Java Café");

        // Apply the calm Cafe theme before creating any UI component
        CafeTheme.applyTheme();
        
        SwingUtilities.invokeLater(() -> {
            // Creates the single main window of the system
            JFrame mainFrame = new JFrame("☕ Java Café - Point of Sale");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1024, 820);

            // Load and set the application window icon image
            try {
                java.io.File iconFile = new java.io.File("imgs/app_icon.png");
                if (iconFile.exists()) {
                    ImageIcon imgIcon = new ImageIcon(iconFile.getAbsolutePath());
                    // Set window icon (standard for Windows/Linux)
                    mainFrame.setIconImage(imgIcon.getImage());
                    
                    // Set macOS Dock icon using AWT Taskbar API if supported
                    if (java.awt.Taskbar.isTaskbarSupported()) {
                        java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                        if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                            taskbar.setIconImage(imgIcon.getImage());
                        }
                    }
                }
            } catch (Exception e) {
                // Fail silently if icon loading fails
            }

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

            // Adds each panel as a different tab with soft emojis
            tabbedPane.addTab("☕ Orders", orderScreen);
            tabbedPane.addTab("📋 Inventory", inventoryScreen);
            tabbedPane.addTab("📈 Sales Reports", reportScreen);

            // Adds the tabbed panel to the main window
            mainFrame.add(tabbedPane);
            
            // Centers and displays the window
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }
}