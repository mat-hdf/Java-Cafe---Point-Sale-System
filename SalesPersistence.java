import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SalesPersistence {
    private static final String FILE_PATH = "sales.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Classe que representa um item vendido
    public static class SaleItem {
        private String name;
        private int quantity;
        private double price;

        public SaleItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }

    // Classe que representa uma transacao completa
    public static class SaleTransaction {
        private String id;
        private LocalDateTime timestamp;
        private List<SaleItem> items;

        public SaleTransaction(String id, LocalDateTime timestamp) {
            this.id = id;
            this.timestamp = timestamp;
            this.items = new ArrayList<>();
        }

        public String getId() { return id; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public List<SaleItem> getItems() { return items; }
        public void addItem(SaleItem item) { items.add(item); }

        public double getTotal() {
            double total = 0;
            for (SaleItem item : items) {
                total += item.getQuantity() * item.getPrice();
            }
            return total;
        }
    }

    // Carrega todas as vendas do arquivo CSV
    public static List<SaleTransaction> loadSales() {
        List<SaleTransaction> transactions = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            generateMockData();
        }

        Map<String, SaleTransaction> transactionMap = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Pula o cabecalho
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String id = parts[0];
                LocalDateTime timestamp = LocalDateTime.parse(parts[1], FORMATTER);
                String name = parts[2];
                int quantity = Integer.parseInt(parts[3]);
                double price = Double.parseDouble(parts[4]);

                SaleTransaction tx = transactionMap.computeIfAbsent(id, k -> new SaleTransaction(k, timestamp));
                tx.addItem(new SaleItem(name, quantity, price));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(transactionMap.values());
    }

    // Salva uma nova venda no arquivo CSV
    public static void saveSale(List<SaleItem> items) {
        if (items == null || items.isEmpty()) return;

        String id = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        File file = new File(FILE_PATH);
        boolean writeHeader = !file.exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                pw.println("transaction_id,timestamp,item_name,quantity,price");
            }
            for (SaleItem item : items) {
                pw.printf("%s,%s,%s,%d,%.2f\n",
                    id,
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

    // Gera dados ficticios se o arquivo nao existir
    private static void generateMockData() {
        File file = new File(FILE_PATH);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("transaction_id,timestamp,item_name,quantity,price");

            LocalDateTime now = LocalDateTime.now();
            String id1 = UUID.randomUUID().toString();
            String id2 = UUID.randomUUID().toString();
            String id3 = UUID.randomUUID().toString();

            // Venda 1: Hoje
            pw.printf("%s,%s,%s,%d,%.2f\n", id1, now.format(FORMATTER), "Coffee", 2, 5.00);
            pw.printf("%s,%s,%s,%d,%.2f\n", id1, now.format(FORMATTER), "Pie", 1, 15.50);

            // Venda 2: 2 dias atras (Semana atual e Mes atual)
            LocalDateTime twoDaysAgo = now.minusDays(2);
            pw.printf("%s,%s,%s,%d,%.2f\n", id2, twoDaysAgo.format(FORMATTER), "Cake", 1, 12.00);
            pw.printf("%s,%s,%s,%d,%.2f\n", id2, twoDaysAgo.format(FORMATTER), "Coffee", 1, 5.00);

            // Venda 3: 10 dias atras (Apenas Mes atual)
            LocalDateTime tenDaysAgo = now.minusDays(10);
            pw.printf("%s,%s,%s,%d,%.2f\n", id3, tenDaysAgo.format(FORMATTER), "Capuccino", 3, 6.00);
            pw.printf("%s,%s,%s,%d,%.2f\n", id3, tenDaysAgo.format(FORMATTER), "Pie", 2, 15.50);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
