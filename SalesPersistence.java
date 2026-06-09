import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles persistence for the sales system, including loading and saving
 * sales transactions to a CSV file.
 */
public class SalesPersistence {
    private static final String FILE_PATH = "sales.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Represents an item that was sold, containing its name, quantity, and unit price.
     */
    public static class SaleItem {
        private String name;
        private int quantity;
        private double price;

        /**
         * Constructs a new SaleItem.
         *
         * @param name     the name of the item
         * @param quantity the quantity sold
         * @param price    the unit price
         */
        public SaleItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        /**
         * Gets the item name.
         *
         * @return the item name
         */
        public String getName() { return name; }

        /**
         * Gets the quantity sold.
         *
         * @return the quantity
         */
        public int getQuantity() { return quantity; }

        /**
         * Gets the unit price.
         *
         * @return the price
         */
        public double getPrice() { return price; }
    }

    /**
     * Represents a complete sale transaction, containing a timestamp and the list of items sold.
     */
    public static class SaleTransaction {
        private LocalDateTime timestamp;
        private List<SaleItem> items;

        /**
         * Constructs a new SaleTransaction.
         *
         * @param timestamp the date and time of the transaction
         */
        public SaleTransaction(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            this.items = new ArrayList<>();
        }

        /**
         * Gets the transaction timestamp.
         *
         * @return the timestamp
         */
        public LocalDateTime getTimestamp() { return timestamp; }

        /**
         * Gets the list of items in this transaction.
         *
         * @return the list of sale items
         */
        public List<SaleItem> getItems() { return items; }

        /**
         * Adds an item to the transaction.
         *
         * @param item the sale item to add
         */
        public void addItem(SaleItem item) { items.add(item); }

        /**
         * Calculates the total price of all items in this transaction.
         *
         * @return the total transaction value
         */
        public double getTotal() {
            double total = 0;
            for (SaleItem item : items) {
                total += item.getQuantity() * item.getPrice();
            }
            return total;
        }
    }

    /**
     * Loads all sale transactions from the CSV file.
     * Generates mock data if the file does not exist.
     *
     * @return a list of loaded SaleTransactions
     */
    public static List<SaleTransaction> loadSales() {
        List<SaleTransaction> transactions = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            generateMockData();
        }

        Map<LocalDateTime, SaleTransaction> transactionMap = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Pula o cabecalho
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                LocalDateTime timestamp = LocalDateTime.parse(parts[0], FORMATTER);
                String name = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                double price = Double.parseDouble(parts[3]);

                SaleTransaction tx = transactionMap.computeIfAbsent(timestamp, k -> new SaleTransaction(k));
                tx.addItem(new SaleItem(name, quantity, price));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(transactionMap.values());
    }

    /**
     * Appends a new sale transaction to the CSV file.
     *
     * @param items the list of items sold in the transaction
     */
    public static void saveSale(List<SaleItem> items) {
        if (items == null || items.isEmpty()) return;

        LocalDateTime timestamp = LocalDateTime.now();
        File file = new File(FILE_PATH);
        boolean writeHeader = !file.exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                pw.println("timestamp,item_name,quantity,price");
            }
            for (SaleItem item : items) {
                pw.printf("%s,%s,%d,%.2f\n",
                    timestamp.format(FORMATTER),
                    item.getName().replace(",", " "),
                    item.getQuantity(),
                    item.getPrice()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates mock sales data and writes it to a new CSV file.
     */
    private static void generateMockData() {
        File file = new File(FILE_PATH);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("timestamp,item_name,quantity,price");

            LocalDateTime now = LocalDateTime.now();

            // Venda 1: Hoje
            pw.printf("%s,%s,%d,%.2f\n", now.format(FORMATTER), "Coffee", 2, 5.00);
            pw.printf("%s,%s,%d,%.2f\n", now.format(FORMATTER), "Pie", 1, 15.50);

            // Venda 2: 2 dias atras (Semana atual e Mes atual)
            LocalDateTime twoDaysAgo = now.minusDays(2);
            pw.printf("%s,%s,%d,%.2f\n", twoDaysAgo.format(FORMATTER), "Cake", 1, 12.00);
            pw.printf("%s,%s,%d,%.2f\n", twoDaysAgo.format(FORMATTER), "Coffee", 1, 5.00);

            // Venda 3: 10 dias atras (Apenas Mes atual)
            LocalDateTime tenDaysAgo = now.minusDays(10);
            pw.printf("%s,%s,%d,%.2f\n", tenDaysAgo.format(FORMATTER), "Capuccino", 3, 6.00);
            pw.printf("%s,%s,%d,%.2f\n", tenDaysAgo.format(FORMATTER), "Pie", 2, 15.50);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
