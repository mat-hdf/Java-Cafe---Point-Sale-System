import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import javax.swing.*;

/**
 * Controller class for the Sales Report Dashboard.
 * Handles loading transactions, filtering by period, calculating best sellers, and exporting summaries.
 */
public class SalesReportLogic implements ActionListener {
    private SalesReportGUI gui;
    private List<SalesPersistence.SaleTransaction> allTransactions;
    private List<SalesPersistence.SaleTransaction> filteredTransactions;
    private List<Map.Entry<String, Integer>> topItems;
    private double totalRevenue;

    /**
     * Constructs a new SalesReportLogic controller linked to a SalesReportGUI dashboard.
     *
     * @param gui the SalesReportGUI dashboard instance
     */
    public SalesReportLogic(SalesReportGUI gui) {
        this.gui = gui;
        refresh();
    }

    /**
     * Reloads sales data from the persistence layer and updates the dashboard.
     */
    public void refresh() {
        allTransactions = SalesPersistence.loadSales();
        updateReport();
    }

    /**
     * Re-calculates and updates all metrics on the dashboard based on the selected period.
     */
    private void updateReport() {
        String selectedPeriod = (String) gui.getPeriodComboBox().getSelectedItem();
        filteredTransactions = filterTransactions(allTransactions, selectedPeriod);

        // Calcula a receita total
        totalRevenue = 0;
        for (SalesPersistence.SaleTransaction tx : filteredTransactions) {
            totalRevenue += tx.getTotal();
        }

        // Calcula os itens mais vendidos
        topItems = calculateTopItems(filteredTransactions);

        // Atualiza a tela
        gui.setRevenue(totalRevenue);
        gui.setTransactions(filteredTransactions.size());
        gui.setTopItems(topItems);
    }

    /**
     * Filters a list of sales transactions by a selected period name.
     *
     * @param all    the list of all transactions
     * @param period the period name ("Today", "Current Week", or "Current Month")
     * @return the filtered list of transactions
     */
    private List<SalesPersistence.SaleTransaction> filterTransactions(List<SalesPersistence.SaleTransaction> all, String period) {
        List<SalesPersistence.SaleTransaction> filtered = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (SalesPersistence.SaleTransaction tx : all) {
            LocalDate txDate = tx.getTimestamp().toLocalDate();
            switch (period) {
                case "Today":
                    if (txDate.equals(today)) {
                        filtered.add(tx);
                    }
                    break;
                case "Current Week":
                    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    if (!txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)) {
                        filtered.add(tx);
                    }
                    break;
                case "Current Month":
                    if (txDate.getYear() == today.getYear() && txDate.getMonth() == today.getMonth()) {
                        filtered.add(tx);
                    }
                    break;
            }
        }
        return filtered;
    }

    /**
     * Groups and calculates the top best-selling items from a list of transactions.
     *
     * @param transactions list of transactions
     * @return list of best sellers sorted by total quantity sold, capped at 3 items
     */
    private List<Map.Entry<String, Integer>> calculateTopItems(List<SalesPersistence.SaleTransaction> transactions) {
        Map<String, Integer> counts = new HashMap<>();
        for (SalesPersistence.SaleTransaction tx : transactions) {
            for (SalesPersistence.SaleItem item : tx.getItems()) {
                counts.put(item.getName(), counts.getOrDefault(item.getName(), 0) + item.getQuantity());
            }
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (sorted.size() > 3) {
            return sorted.subList(0, 3);
        }
        return sorted;
    }

    /**
     * Listens for action events triggered from GUI control buttons or JComboBox filters.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == gui.getPeriodComboBox()) {
            updateReport();
        } else if (e.getActionCommand().equals("Refresh")) {
            refresh();
            JOptionPane.showMessageDialog(gui, "Sales data refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("Export Report")) {
            exportReport();
        }
    }

    /**
     * Exports the current dashboard report overview to a text or CSV file.
     */
    private void exportReport() {
        if (filteredTransactions == null) return;

        String selectedPeriod = (String) gui.getPeriodComboBox().getSelectedItem();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Sales Report");
        fileChooser.setSelectedFile(new File("sales_report_" + selectedPeriod.toLowerCase().replace(" ", "_") + ".txt"));

        int userSelection = fileChooser.showSaveDialog(gui);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();

            // Garante extensao padrao caso nao informada
            if (!path.toLowerCase().endsWith(".txt") && !path.toLowerCase().endsWith(".csv")) {
                path += ".txt";
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
                if (path.toLowerCase().endsWith(".csv")) {
                    writer.println("Period,Metric,Value");
                    writer.println(selectedPeriod + ",Total Revenue," + String.format("%.2f", totalRevenue));
                    writer.println(selectedPeriod + ",Number of Transactions," + filteredTransactions.size());
                    for (int i = 0; i < topItems.size(); i++) {
                        Map.Entry<String, Integer> entry = topItems.get(i);
                        writer.println(selectedPeriod + ",Top " + (i + 1) + " Item," + entry.getKey() + " (" + entry.getValue() + ")");
                    }
                } else {
                    writer.println("=========================================");
                    writer.println("          SALES REPORT SUMMARY");
                    writer.println("=========================================");
                    writer.println("Period: " + selectedPeriod);
                    writer.println("Generated At: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    writer.println("-----------------------------------------");
                    writer.println("Total Revenue: $" + String.format("%.2f", totalRevenue));
                    writer.println("Total Transactions: " + filteredTransactions.size());
                    writer.println("-----------------------------------------");
                    writer.println("TOP BEST-SELLING ITEMS:");
                    for (int i = 0; i < topItems.size(); i++) {
                        Map.Entry<String, Integer> entry = topItems.get(i);
                        writer.println((i + 1) + ". " + entry.getKey() + " - " + entry.getValue() + " sold");
                    }
                    writer.println("=========================================");
                }
                JOptionPane.showMessageDialog(gui, "Report exported successfully to:\n" + path, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gui, "Error writing file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
