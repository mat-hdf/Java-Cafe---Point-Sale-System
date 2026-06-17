import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

/**
 * Custom zero-dependency test runner to run unit tests on SalesPersistence,
 * SalesReportGUI, OrderLogic, and InventoryLogic.
 */
public class TestRunner {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("       RUNNING JAVA CAFE UNIT TESTS      ");
        System.out.println("=========================================");

        // Backup existing sales.csv
        File backupFile = new File("sales.csv.bak");
        File salesFile = new File("sales.csv");
        boolean backedUp = false;

        if (salesFile.exists()) {
            backedUp = salesFile.renameTo(backupFile);
            if (!backedUp) {
                System.err.println("Warning: Could not back up sales.csv. Tests might overwrite existing data.");
            }
        }

        try {
            // Run test suites
            runTest("SalesPersistence Save and Load", TestRunner::testSalesPersistence);
            runTest("SalesReportGUI Filtering and Metrics", TestRunner::testSalesReportGUI);
            runTest("OrderLogic Item Management", TestRunner::testOrderLogic);
            runTest("InventoryLogic Operations", TestRunner::testInventoryLogic);

        } finally {
            // Restore backup
            if (salesFile.exists()) {
                salesFile.delete();
            }
            if (backedUp && backupFile.exists()) {
                backupFile.renameTo(salesFile);
            }
        }

        System.out.println("=========================================");
        System.out.printf("Test Summary: %d/%d tests passed.\n", passedTests, totalTests);
        System.out.println("=========================================");

        if (passedTests < totalTests) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private static void runTest(String testName, RunnableTest test) {
        totalTests++;
        System.out.print("Running test: " + testName + " ... ");
        try {
            test.run();
            passedTests++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            System.out.println("FAILED");
            t.printStackTrace();
        }
    }

    @FunctionalInterface
    interface RunnableTest {
        void run() throws Exception;
    }

    // --- TESTS ---

    private static void testSalesPersistence() throws Exception {
        // Prepare mock items
        List<SalesPersistence.SaleItem> items = new ArrayList<>();
        items.add(new SalesPersistence.SaleItem("Espresso", 3, 2.50));
        items.add(new SalesPersistence.SaleItem("Muffin", 1, 4.00));

        // Save sale
        SalesPersistence.saveSale(items);

        // Load sales
        List<SalesPersistence.SaleTransaction> transactions = SalesPersistence.loadSales();
        assertEqual(1, transactions.size(), "Should have exactly 1 transaction saved");

        SalesPersistence.SaleTransaction tx = transactions.get(0);
        assertEqual(11.50, tx.getTotal(), "Total price should be 3 * 2.50 + 1 * 4.00 = 11.50");
        assertEqual(2, tx.getItems().size(), "Transaction should contain 2 items");

        assertEqual("Espresso", tx.getItems().get(0).getName(), "First item name matches");
        assertEqual(3, tx.getItems().get(0).getQuantity(), "First item quantity matches");
        assertEqual(2.50, tx.getItems().get(0).getPrice(), "First item price matches");
    }

    private static void testSalesReportGUI() throws Exception {
        // Setup initial CSV with known historical values
        File salesFile = new File("sales.csv");
        if (salesFile.exists()) {
            salesFile.delete();
        }

        // We write mock transactions directly using the persistence framework mock generation or manual save
        List<SalesPersistence.SaleItem> items1 = new ArrayList<>();
        items1.add(new SalesPersistence.SaleItem("Pie", 2, 4.50)); // $9.00
        SalesPersistence.saveSale(items1); // Saved now (Today)

        // Instantiate components
        SalesReportGUI gui = new SalesReportGUI();

        // Retrieve private fields for assertions using reflection
        JLabel revenueLabel = getPrivateField(gui, "revenueValueLabel");
        JLabel transactionsLabel = getPrivateField(gui, "transactionsValueLabel");

        // Verify "Today" (default selection or selected manually)
        gui.getPeriodComboBox().setSelectedItem("Today");
        assertEqual("$ 9.00", revenueLabel.getText(), "Today revenue should be $ 9.00");
        assertEqual("1", transactionsLabel.getText(), "Today transactions count should be 1");
    }

    private static void testOrderLogic() throws Exception {
        OrderGUI gui = new OrderGUI();
        OrderLogic logic = new OrderLogic(gui);
        gui.setController(logic);

        // Simulate button clicks
        DefaultTableModel model = gui.getTableModel();
        assertEqual(0, model.getRowCount(), "Table should start empty");

        // Use reflection or invoke ActionEvents directly on controller
        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Pie"));
        assertEqual(1, model.getRowCount(), "Table should contain 1 item after adding Pie");
        assertEqual("Pie", model.getValueAt(0, 1), "Item added should be Pie");

        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Coffee"));
        assertEqual(2, model.getRowCount(), "Table should contain 2 items after adding Coffee");

        // Verify total label
        assertEqual("7.70", gui.getOrderValueLabel().getText(), "Total value should be 4.50 + 2.50 = 7.00 + 10% tax = 7.70");

        // Cancel order
        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Cancel Order"));
        assertEqual(0, model.getRowCount(), "Table should be empty after cancellation");
        assertEqual("0.00", gui.getOrderValueLabel().getText(), "Total value should reset to 0.00");
    }

    private static void testInventoryLogic() throws Exception {
        InventoryGUI gui = new InventoryGUI();
        InventoryLogic logic = new InventoryLogic(gui);
        gui.setController(logic);

        DefaultTableModel model = gui.getTableModel();
        int initialRowCount = model.getRowCount();

        // Populate fields to simulate adding product
        gui.getNameField().setText("Cookie");
        gui.getPriceField().setText("1.50");
        gui.getStockField().setText("20");

        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Add Product"));
        assertEqual(initialRowCount + 1, model.getRowCount(), "Inventory should increase by 1 product");
        assertEqual("Cookie", model.getValueAt(model.getRowCount() - 1, 0), "Last added product name matches");
        assertEqual("1.50", model.getValueAt(model.getRowCount() - 1, 1), "Last added product price matches");
        assertEqual("20", model.getValueAt(model.getRowCount() - 1, 2), "Last added product stock matches");
    }

    // --- ASSERTION HELPERS ---

    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    private static void assertEqual(double expected, double actual, String message) {
        if (Math.abs(expected - actual) < 0.001) return;
        throw new AssertionError(message + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
