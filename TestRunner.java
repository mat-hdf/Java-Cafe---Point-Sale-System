import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

/**
 * Custom zero-dependency test runner designed to validate Java Cafe unit tests.
 * Performs integration and unit tests on SalesPersistence, SalesReportGUI, OrderLogic,
 * and InventoryLogic. Ensures UI controllers route action events correctly, date filters
 * query boundaries accurately, and data persistence parses CSV records properly.
 */
public class TestRunner {

    /** Total count of tests executed during the suite run. */
    private static int totalTests = 0;
    
    /** Count of test cases that completed successfully without throwing exceptions. */
    private static int passedTests = 0;

    /**
     * Entry point of the test runner. 
     * Manages test environment sandbox setups (backing up and restoring existing sales.csv data),
     * triggers individual test suites, reports results, and exits with appropriate status codes.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("       RUNNING JAVA CAFE UNIT TESTS      ");
        System.out.println("=========================================");

        // Define backup files to isolate test operations from actual user data
        File salesBackup = new File("sales.csv.bak");
        File salesFile = new File("sales.csv");
        boolean salesBackedUp = false;

        // Perform backup if an active sales.csv file already exists
        if (salesFile.exists()) {
            salesBackedUp = salesFile.renameTo(salesBackup);
            if (!salesBackedUp) {
                System.err.println("Warning: Could not back up sales.csv. Tests might overwrite existing data.");
            }
        }

        File invBackup = new File("inventory.csv.bak");
        File invFile = new File("inventory.csv");
        boolean invBackedUp = false;

        // Perform backup if an active inventory.csv file already exists
        if (invFile.exists()) {
            invBackedUp = invFile.renameTo(invBackup);
            if (!invBackedUp) {
                System.err.println("Warning: Could not back up inventory.csv. Tests might overwrite existing data.");
            }
        }

        try {
            // Run the test suites
            runTest("SalesPersistence Save and Load", TestRunner::testSalesPersistence);
            runTest("SalesReportGUI Filtering and Metrics", TestRunner::testSalesReportGUI);
            runTest("OrderLogic Item Management", TestRunner::testOrderLogic);
            runTest("InventoryLogic Operations", TestRunner::testInventoryLogic);

        } finally {
            // Cleanup test-generated sales file
            if (salesFile.exists()) {
                salesFile.delete();
            }
            // Restore original user sales.csv file from backup if it was moved
            if (salesBackedUp && salesBackup.exists()) {
                salesBackup.renameTo(salesFile);
            }

            // Cleanup test-generated inventory file
            if (invFile.exists()) {
                invFile.delete();
            }
            // Restore original user inventory.csv file from backup if it was moved
            if (invBackedUp && invBackup.exists()) {
                invBackup.renameTo(invFile);
            }
        }

        System.out.println("=========================================");
        System.out.printf("Test Summary: %d/%d tests passed.\n", passedTests, totalTests);
        System.out.println("=========================================");

        // Exit with failure code 1 if any tests failed, otherwise exit with success code 0
        if (passedTests < totalTests) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    /**
     * Helper method to execute a single test case block.
     * Tracks execution counts, prints statuses, and catches AssertionErrors or exceptions.
     *
     * @param testName name of the test to display
     * @param test     the test code lambda block containing assertions
     */
    private static void runTest(String testName, RunnableTest test) {
        totalTests++;
        System.out.print("Running test: " + testName + " ... ");
        try {
            test.run();
            passedTests++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            System.out.println("FAILED");
            t.printStackTrace(); // Output full stack trace to pinpoint error location
        }
    }

    /**
     * Functional interface representing a test case method block that can throw exceptions.
     */
    @FunctionalInterface
    interface RunnableTest {
        /**
         * Executes the test case.
         *
         * @throws Exception if an assertion fails or an error occurs during execution
         */
        void run() throws Exception;
    }

    // --- TESTS ---

    /**
     * Tests the sales persistence saving and reading features.
     * Generates a sample transaction list, saves it to sales.csv, loads it back,
     * and asserts that values (price, name, quantities) match the original list exactly.
     *
     * @throws Exception if an assertion fails or a file I/O error occurs
     */
    private static void testSalesPersistence() throws Exception {
        // Prepare mock items
        List<SalesPersistence.SaleItem> items = new ArrayList<>();
        items.add(new SalesPersistence.SaleItem("Espresso", 3, 2.50));
        items.add(new SalesPersistence.SaleItem("Muffin", 1, 4.00));

        // Save mock transaction
        SalesPersistence.saveSale(items);

        // Load sales back from files
        List<SalesPersistence.SaleTransaction> transactions = SalesPersistence.loadSales();
        assertEqual(1, transactions.size(), "Should have exactly 1 transaction saved");

        // Validate details
        SalesPersistence.SaleTransaction tx = transactions.get(0);
        assertEqual(11.50, tx.getTotal(), "Total price should be 3 * 2.50 + 1 * 4.00 = 11.50");
        assertEqual(2, tx.getItems().size(), "Transaction should contain 2 items");

        assertEqual("Espresso", tx.getItems().get(0).getName(), "First item name matches");
        assertEqual(3, tx.getItems().get(0).getQuantity(), "First item quantity matches");
        assertEqual(2.50, tx.getItems().get(0).getPrice(), "First item price matches");
    }

    /**
     * Tests the filtering logic and dashboard component of SalesReportGUI.
     * Writes sample sales transactions, instantiates SalesReportGUI, selects time periods,
     * and validates that UI label outputs correspond to the filtered data totals.
     *
     * @throws Exception if an assertion fails or a reflection mapping fails
     */
    private static void testSalesReportGUI() throws Exception {
        // Setup initial clean CSV state for sales data
        File salesFile = new File("sales.csv");
        if (salesFile.exists()) {
            salesFile.delete();
        }

        // Save a mock sale to test "Today" filtering
        List<SalesPersistence.SaleItem> items1 = new ArrayList<>();
        items1.add(new SalesPersistence.SaleItem("Pie", 2, 4.50)); // Total: $9.00
        SalesPersistence.saveSale(items1);

        // Instantiate sales dashboard GUI component
        SalesReportGUI gui = new SalesReportGUI();

        // Extract private JLabels using Java Reflection helper
        JLabel revenueLabel = getPrivateField(gui, "revenueValueLabel");
        JLabel transactionsLabel = getPrivateField(gui, "transactionsValueLabel");

        // Set period filter to "Today" to trigger aggregation calculations
        gui.getPeriodComboBox().setSelectedItem("Today");
        assertEqual("$ 9.00", revenueLabel.getText(), "Today revenue should be $ 9.00");
        assertEqual("1", transactionsLabel.getText(), "Today transactions count should be 1");
    }

    /**
     * Tests standard ordering logic and calculations in OrderLogic and OrderGUI.
     * Simulates GUI button clicks (Pie, Coffee, Cancel) and verifies subtotal sums,
     * tax applications (+10%), and clean order cancel states.
     *
     * @throws Exception if an assertion fails or controllers fail to route actions
     */
    private static void testOrderLogic() throws Exception {
        OrderGUI gui = new OrderGUI();
        OrderLogic logic = new OrderLogic(gui);
        gui.setController(logic);

        // Bind mock inventory logic to resolve item prices
        InventoryGUI invGui = new InventoryGUI();
        InventoryLogic invLogic = new InventoryLogic(invGui);
        logic.setInventoryLogic(invLogic);

        // Ensure table starts completely empty
        DefaultTableModel model = gui.getTableModel();
        assertEqual(0, model.getRowCount(), "Table should start empty");

        // Simulate choosing "Pie" from the catalog panel
        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Pie"));
        assertEqual(1, model.getRowCount(), "Table should contain 1 item after adding Pie");
        assertEqual("Pie", model.getValueAt(0, 1), "Item added should be Pie");

        // Simulate choosing "Coffee" from the catalog panel
        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Coffee"));
        assertEqual(2, model.getRowCount(), "Table should contain 2 items after adding Coffee");

        // Verify total label calculations (4.50 Pie + 2.50 Coffee = 7.00 + 10% tax = 7.70)
        assertEqual("7.70", gui.getOrderValueLabel().getText(), "Total value should be 4.50 + 2.50 = 7.00 + 10% tax = 7.70");

        // Simulate cancelling order action
        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Cancel Order"));
        assertEqual(0, model.getRowCount(), "Table should be empty after cancellation");
        assertEqual("0.00", gui.getOrderValueLabel().getText(), "Total value should reset to 0.00");
    }

    /**
     * Tests inventory updates and additions in InventoryLogic and InventoryGUI.
     * Populates input text/spinner controls with new product parameters, sends
     * the "Add Product" action, and verifies database model expansion.
     *
     * @throws Exception if an assertion fails or product addition fails
     */
    private static void testInventoryLogic() throws Exception {
        InventoryGUI gui = new InventoryGUI();
        InventoryLogic logic = new InventoryLogic(gui);
        gui.setController(logic);

        DefaultTableModel model = gui.getTableModel();
        int initialRowCount = model.getRowCount();

        // Populate fields to simulate adding product "Cookie"
        gui.getNameField().setText("Cookie");
        gui.getPriceField().setText("1.50");
        gui.getStockField().setText("20");

        // Trigger action command
        logic.actionPerformed(new java.awt.event.ActionEvent(gui, 0, "Add Product"));
        
        // Assert inventory records increased by 1 and matching values exist at the bottom of the table
        assertEqual(initialRowCount + 1, model.getRowCount(), "Inventory should increase by 1 product");
        assertEqual("Cookie", model.getValueAt(model.getRowCount() - 1, 0), "Last added product name matches");
        assertEqual("1.50", model.getValueAt(model.getRowCount() - 1, 1), "Last added product price matches");
        assertEqual("20", model.getValueAt(model.getRowCount() - 1, 2), "Last added product stock matches");

        // Clean up: delete the test entry "Cookie" after verification to keep database clean
        model.removeRow(model.getRowCount() - 1);
    }

    // --- ASSERTION HELPERS ---

    /**
     * Compares two objects for equality. Throws AssertionError if they do not match.
     *
     * @param expected expected value
     * @param actual   actual value
     * @param message  context message describing the assertion
     */
    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    /**
     * Compares two double-precision values for equality within a small delta range (0.001).
     * Throws AssertionError if they do not match.
     *
     * @param expected expected double value
     * @param actual   actual double value
     * @param message  context message describing the assertion
     */
    private static void assertEqual(double expected, double actual, String message) {
        if (Math.abs(expected - actual) < 0.001) return;
        throw new AssertionError(message + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    /**
     * Reflection helper to extract private variables/fields from components during tests.
     *
     * @param <T>       expected field return type
     * @param obj       the object instance containing the field
     * @param fieldName name of the private field to access
     * @return the value of the field
     * @throws Exception if reflection access is denied or the field is missing
     */
    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
