import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Graphical User Interface for the Sales Performance Dashboard.
 * Displays key metrics like total revenue, transactions count, and the top-3 best selling items.
 * Implements ActionListener to handle event calculations and updates dynamically.
 */
public class SalesReportGUI extends JPanel implements ActionListener {
    
    /** Combo box selector for filtering sales by period (Today, Current Week, Current Month). */
    private JComboBox<String> periodComboBox;
    
    /** Button to reload sales data from persistence. */
    private JButton refreshButton;
    
    /** Button to export the sales metrics to a CSV or TXT file. */
    private JButton exportButton;

    /** Label that displays the calculated total revenue amount. */
    private JLabel revenueValueLabel;
    
    /** Label that displays the calculated transaction count. */
    private JLabel transactionsValueLabel;
    
    /** Array of labels that display the top-3 best selling items. */
    private JLabel[] topItemLabels;

    /** In-memory collection of all loaded transaction records. */
    private List<SalesPersistence.SaleTransaction> allTransactions;
    
    /** Filtered subset of transactions matching the currently selected period. */
    private List<SalesPersistence.SaleTransaction> filteredTransactions;
    
    /** Sorted list of best-selling products mapping product names to quantity sold. */
    private List<Map.Entry<String, Integer>> topItems;
    
    /** Calculated total revenue for the current filtered period. */
    private double totalRevenue;

    /**
     * Constructs a new SalesReportGUI dashboard with metric cards and action buttons,
     * and initializes report sales data.
     */
    public SalesReportGUI() {
        // Configure the main layout with external margins and a warm off-white background
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(24, 24, 24, 24));
        setBackground(CafeTheme.OFF_WHITE);

        // Top control panel containing the title and actions block
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false);

        // Title and description block
        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 2, 2));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("📊 Sales Performance");
        titleLabel.setFont(CafeTheme.TITLE_FONT);
        titleLabel.setForeground(CafeTheme.DARK_ROAST);
        
        JLabel subtitleLabel = new JLabel("View cafe sales metrics, transactions, and best sellers");
        subtitleLabel.setFont(CafeTheme.REGULAR_FONT);
        subtitleLabel.setForeground(CafeTheme.TEXT_MUTED);
        
        titleContainer.add(titleLabel);
        titleContainer.add(subtitleLabel);
        controlPanel.add(titleContainer, BorderLayout.WEST);

        // Right-aligned actions panel for filters and button controls
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        // Period filter combo box selector
        periodComboBox = new JComboBox<>(new String[]{"Today", "Current Week", "Current Month"});
        periodComboBox.setFont(CafeTheme.REGULAR_FONT);
        periodComboBox.setPreferredSize(new Dimension(150, 36));
        periodComboBox.setBackground(Color.WHITE);
        periodComboBox.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        periodComboBox.setFocusable(false);

        // Refresh button triggering data reload
        refreshButton = new CafeTheme.CafeButton("Refresh", CafeTheme.CafeButton.Variant.PRIMARY);
        refreshButton.setPreferredSize(new Dimension(100, 36));

        // Export report button
        exportButton = new CafeTheme.CafeButton("Export Report", CafeTheme.CafeButton.Variant.SECONDARY);
        exportButton.setPreferredSize(new Dimension(140, 36));

        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(CafeTheme.BOLD_FONT);
        periodLabel.setForeground(CafeTheme.TEXT_MUTED);

        actionsPanel.add(periodLabel);
        actionsPanel.add(periodComboBox);
        actionsPanel.add(refreshButton);
        actionsPanel.add(exportButton);

        controlPanel.add(actionsPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.NORTH);

        // Central panel containing the metric visualization cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 24, 0));
        cardsPanel.setOpaque(false);

        // Card 1: Total Revenue (styled in Olive Green success color)
        ShadowCard revenueCard = createMetricCard("TOTAL REVENUE", CafeTheme.SUCCESS_OLIVE, "💵");
        revenueValueLabel = new JLabel("$ 0.00");
        revenueValueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        revenueValueLabel.setForeground(CafeTheme.SUCCESS_OLIVE);
        revenueValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        revenueCard.add(revenueValueLabel, BorderLayout.CENTER);

        // Card 2: Transactions Count (styled in Caramel brand color)
        ShadowCard transactionsCard = createMetricCard("TRANSACTIONS", CafeTheme.CARAMEL, "🧾");
        transactionsValueLabel = new JLabel("0");
        transactionsValueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        transactionsValueLabel.setForeground(CafeTheme.CARAMEL);
        transactionsValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transactionsCard.add(transactionsValueLabel, BorderLayout.CENTER);

        // Card 3: Top-3 Best Selling Items (styled in Soft Orange accent color)
        ShadowCard topSellingCard = createMetricCard("TOP 3 BEST SELLERS", CafeTheme.ORANGE_SOFT, "🏆");
        JPanel topItemsListPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        topItemsListPanel.setOpaque(false);
        
        topItemLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            topItemLabels[i] = new JLabel("-");
            topItemLabels[i].setFont(CafeTheme.REGULAR_FONT);
            topItemLabels[i].setForeground(CafeTheme.TEXT_MUTED);
            topItemLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            topItemsListPanel.add(topItemLabels[i]);
        }
        topSellingCard.add(topItemsListPanel, BorderLayout.CENTER);

        cardsPanel.add(revenueCard);
        cardsPanel.add(transactionsCard);
        cardsPanel.add(topSellingCard);

        add(cardsPanel, BorderLayout.CENTER);

        // Register action listeners directly
        periodComboBox.addActionListener(this);
        refreshButton.addActionListener(this);
        exportButton.addActionListener(this);

        // Initial loading of metrics
        refresh();
    }

    /**
     * Helper method to create a modern looking visual metric card.
     * Sets up a header containing an icon and title, wrapped in a ShadowCard layout.
     *
     * @param title       the header title of the card
     * @param accentColor the top horizontal highlight accent line color
     * @param icon        the emoji icon string representing the metric
     * @return the configured ShadowCard instance
     */
    private ShadowCard createMetricCard(String title, Color accentColor, String icon) {
        ShadowCard card = new ShadowCard(16, accentColor);

        // Header panel containing Icon + Title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CafeTheme.BOLD_FONT);
        titleLabel.setForeground(CafeTheme.TEXT_MUTED);

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        card.add(headerPanel, BorderLayout.NORTH);

        return card;
    }

    /**
     * Reloads sales data from the persistence layer and updates the dashboard metrics.
     */
    public void refresh() {
        allTransactions = SalesPersistence.loadSales();
        updateReport();
    }

    /**
     * Re-calculates and updates all metrics on the dashboard based on the selected period.
     * Filters transactions, aggregates total revenue, finds top products, and updates labels.
     */
    private void updateReport() {
        String selectedPeriod = (String) getPeriodComboBox().getSelectedItem();
        filteredTransactions = filterTransactions(allTransactions, selectedPeriod);

        // Calculate total revenue
        totalRevenue = 0;
        for (SalesPersistence.SaleTransaction tx : filteredTransactions) {
            totalRevenue += tx.getTotal();
        }

        // Calculate top selling items
        topItems = calculateTopItems(filteredTransactions);

        // Update UI displays
        setRevenue(totalRevenue);
        setTransactions(filteredTransactions.size());
        setTopItems(topItems);
    }

    /**
     * Filters a list of sales transactions by a selected period name.
     * Filters are relative to the current local date (Today, Current Week, or Current Month).
     *
     * @param all    the list of all transactions
     * @param period the period name ("Today", "Current Week", or "Current Month")
     * @return the filtered list of transactions matching the temporal bounds
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
                    // Monday to Sunday temporal bounds
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
     * Sums up quantities sold per item and sorts descending. Caps the result to at most 3 items.
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
        // Sort descending by value (sales count)
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (sorted.size() > 3) {
            return sorted.subList(0, 3);
        }
        return sorted;
    }

    /**
     * Listens for action events triggered from GUI control buttons or JComboBox filters.
     * Handles period filter switches, manual refreshing, and report exporting.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getPeriodComboBox()) {
            updateReport();
        } else if (e.getActionCommand().equals("Refresh")) {
            refresh();
            JOptionPane.showMessageDialog(this, "Sales data refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("Export Report")) {
            exportReport();
        }
    }

    /**
     * Exports the current dashboard report overview to a text (.txt) or CSV (.csv) file.
     * Asks the user for a destination path using a save dialog.
     */
    private void exportReport() {
        if (filteredTransactions == null) return;

        String selectedPeriod = (String) getPeriodComboBox().getSelectedItem();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Sales Report");
        fileChooser.setSelectedFile(new File("sales_report_" + selectedPeriod.toLowerCase().replace(" ", "_") + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();

            // Ensure a default file extension if none is explicitly provided by the user
            if (!path.toLowerCase().endsWith(".txt") && !path.toLowerCase().endsWith(".csv")) {
                path += ".txt";
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
                if (path.toLowerCase().endsWith(".csv")) {
                    // Export in CSV format
                    writer.println("Period,Metric,Value");
                    writer.println(selectedPeriod + ",Total Revenue," + String.format("%.2f", totalRevenue));
                    writer.println(selectedPeriod + ",Number of Transactions," + filteredTransactions.size());
                    for (int i = 0; i < topItems.size(); i++) {
                        Map.Entry<String, Integer> entry = topItems.get(i);
                        writer.println(selectedPeriod + ",Top " + (i + 1) + " Item," + entry.getKey() + " (" + entry.getValue() + ")");
                    }
                } else {
                    // Export in TXT visual table format
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
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + path, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Gets the period selection JComboBox.
     *
     * @return the JComboBox selector
     */
    public JComboBox<String> getPeriodComboBox() { return periodComboBox; }
    
    /**
     * Sets the displayed total revenue amount.
     *
     * @param revenue the total revenue to display
     */
    public void setRevenue(double revenue) {
        revenueValueLabel.setText(String.format("$ %.2f", revenue));
    }
    
    /**
     * Sets the displayed transaction count.
     *
     * @param count the number of transactions to display
     */
    public void setTransactions(int count) {
        transactionsValueLabel.setText(String.valueOf(count));
    }
    
    /**
     * Sets the top-3 best selling items to display in the list.
     * Renders rank badges (#1 Caramel, #2 Soft Orange, #3 Cream Dark) using styled HTML structures.
     *
     * @param topItems list of map entries representing item names and sales quantity
     */
    public void setTopItems(List<Map.Entry<String, Integer>> topItems) {
        String[] rankColors = { "#C47235", "#E68948", "#EEE6DB" }; // Caramel, Soft Orange, Cream Dark
        String[] textColors = { "white", "white", "#452D22" }; // Contrast text colors for badges
        for (int i = 0; i < 3; i++) {
            if (i < topItems.size()) {
                Map.Entry<String, Integer> entry = topItems.get(i);
                topItemLabels[i].setText(String.format(
                    "<html><body style='font-family: SansSerif; font-size: 11px; margin: 3px;'>" +
                    "<span style='background-color: %s; color: %s; padding: 2px 8px; font-weight: bold; border-radius: 4px;'>#%d</span> " +
                    "<b style='color: #452D22;'>%s</b> <font color='#8F7A6D'>(%d sold)</font></body></html>",
                    rankColors[i], textColors[i], i + 1, entry.getKey(), entry.getValue()
                ));
            } else {
                topItemLabels[i].setText("<html><body style='font-family: SansSerif; color: #8F7A6D; font-size: 11px;'>-</body></html>");
            }
        }
    }

    /**
     * Custom JPanel that draws a card with rounded corners and a soft drop shadow.
     * Incorporates an accent horizontal stripe at the top edge.
     */
    private static class ShadowCard extends JPanel {
        private final int cornerRadius;
        private final Color accentColor;
        private final int shadowWidth = 5;

        /**
         * Constructs a new ShadowCard.
         *
         * @param cornerRadius the rounding radius of card corners in pixels
         * @param accentColor  the color of the top edge accent stripe
         */
        public ShadowCard(int cornerRadius, Color accentColor) {
            this.cornerRadius = cornerRadius;
            this.accentColor = accentColor;
            setOpaque(false);
            setBackground(Color.WHITE);
            // Internal margins to prevent content from touching card edges
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            setLayout(new BorderLayout(10, 10));
        }

        /**
         * Renders the soft multi-pass shadow, rounded background panel, and top accent band.
         *
         * @param g the Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = shadowWidth;
            int y = shadowWidth;
            int w = getWidth() - 2 * shadowWidth;
            int h = getHeight() - 2 * shadowWidth;

            // Draw a soft warm espresso-toned drop shadow using progressive multi-pass strokes
            for (int i = 0; i < shadowWidth; i++) {
                // Color uses DARK_ROAST (69, 45, 34) with progressive alpha transparency fade
                g2.setColor(new Color(69, 45, 34, 1 + i * 2));
                g2.drawRoundRect(x - i, y - i, w + 2 * i, h + 2 * i, cornerRadius, cornerRadius);
            }

            // Draw the solid white card background
            g2.setColor(getBackground());
            g2.fillRoundRect(x, y, w, h, cornerRadius, cornerRadius);

            // Draw the horizontal top accent color stripe
            if (accentColor != null) {
                g2.setColor(accentColor);
                g2.fillRoundRect(x, y, w, 6, cornerRadius, cornerRadius);
                // Fill the bottom half of the accent line's rounding area to ensure a flat bottom edge
                g2.fillRect(x, y + 3, w, 3);
            }

            g2.dispose();
        }
    }
}
