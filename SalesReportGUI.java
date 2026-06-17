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
 * Graphical User Interface for the Sales Dashboard.
 * Displays key metrics like total revenue, transactions count, and the top-3 best selling items.
 * Implements ActionListener to handle event calculations and updates dynamically.
 */
public class SalesReportGUI extends JPanel implements ActionListener {
    private JComboBox<String> periodComboBox;
    private JButton refreshButton;
    private JButton exportButton;

    private JLabel revenueValueLabel;
    private JLabel transactionsValueLabel;
    private JLabel[] topItemLabels;

    // Report logic fields
    private List<SalesPersistence.SaleTransaction> allTransactions;
    private List<SalesPersistence.SaleTransaction> filteredTransactions;
    private List<Map.Entry<String, Integer>> topItems;
    private double totalRevenue;

    /**
     * Constructs a new SalesReportGUI dashboard with metric cards and action buttons,
     * and initializes report sales data.
     */
    public SalesReportGUI() {
        // Configura o layout principal com margens externas e fundo moderno slate
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(24, 24, 24, 24));
        setBackground(new Color(248, 250, 252)); // Slate 50 background

        // Painel superior de controle (Titulo e Acoes)
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false);

        // Title and description block
        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 2, 2));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Sales Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42)); // Slate 900
        
        JLabel subtitleLabel = new JLabel("View metrics, transactions, and best sellers");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139)); // Slate 500
        
        titleContainer.add(titleLabel);
        titleContainer.add(subtitleLabel);
        controlPanel.add(titleContainer, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        // Seletor de periodo
        periodComboBox = new JComboBox<>(new String[]{"Today", "Current Week", "Current Month"});
        periodComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        periodComboBox.setPreferredSize(new Dimension(150, 36));
        periodComboBox.setBackground(Color.WHITE);
        periodComboBox.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // Botao de atualizar (Flat Indigo)
        refreshButton = new FlatButton("Refresh", new Color(99, 102, 241), new Color(79, 70, 229), Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(100, 36));

        // Botao de exportar (Flat Slate-Light)
        exportButton = new FlatButton("Export Report", new Color(243, 244, 246), new Color(229, 231, 235), new Color(55, 65, 81));
        exportButton.setPreferredSize(new Dimension(140, 36));

        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        periodLabel.setForeground(new Color(100, 116, 139)); // Slate 500

        actionsPanel.add(periodLabel);
        actionsPanel.add(periodComboBox);
        actionsPanel.add(refreshButton);
        actionsPanel.add(exportButton);

        controlPanel.add(actionsPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.NORTH);

        // Painel central contendo os cards de metricas
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 24, 0));
        cardsPanel.setOpaque(false);

        // Card 1: Receita Total (Verde)
        ShadowCard revenueCard = createMetricCard("TOTAL REVENUE", new Color(16, 185, 129), "💵");
        revenueValueLabel = new JLabel("$ 0.00");
        revenueValueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        revenueValueLabel.setForeground(new Color(16, 185, 129));
        revenueValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        revenueCard.add(revenueValueLabel, BorderLayout.CENTER);

        // Card 2: Numero de Transacoes (Azul)
        ShadowCard transactionsCard = createMetricCard("TRANSACTIONS", new Color(59, 130, 246), "🧾");
        transactionsValueLabel = new JLabel("0");
        transactionsValueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        transactionsValueLabel.setForeground(new Color(59, 130, 246));
        transactionsValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transactionsCard.add(transactionsValueLabel, BorderLayout.CENTER);

        // Card 3: Top-3 Itens (Roxo)
        ShadowCard topSellingCard = createMetricCard("TOP 3 BEST SELLERS", new Color(139, 92, 246), "🏆");
        JPanel topItemsListPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        topItemsListPanel.setOpaque(false);
        
        topItemLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            topItemLabels[i] = new JLabel("-");
            topItemLabels[i].setFont(new Font("Arial", Font.PLAIN, 15));
            topItemLabels[i].setForeground(new Color(71, 85, 105)); // Slate 700
            topItemLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            topItemsListPanel.add(topItemLabels[i]);
        }
        topSellingCard.add(topItemsListPanel, BorderLayout.CENTER);

        cardsPanel.add(revenueCard);
        cardsPanel.add(transactionsCard);
        cardsPanel.add(topSellingCard);

        add(cardsPanel, BorderLayout.CENTER);

        // Wire action listeners directly to self
        periodComboBox.addActionListener(this);
        refreshButton.addActionListener(this);
        exportButton.addActionListener(this);

        // Initial data loading
        refresh();
    }

    /**
     * Helper method to create a modern looking visual metric card.
     */
    private ShadowCard createMetricCard(String title, Color accentColor, String icon) {
        ShadowCard card = new ShadowCard(16, accentColor);

        // Header panel containing Icon + Title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(100, 116, 139)); // Slate 500

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        card.add(headerPanel, BorderLayout.NORTH);

        return card;
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
        String selectedPeriod = (String) getPeriodComboBox().getSelectedItem();
        filteredTransactions = filterTransactions(allTransactions, selectedPeriod);

        // Calcula a receita total
        totalRevenue = 0;
        for (SalesPersistence.SaleTransaction tx : filteredTransactions) {
            totalRevenue += tx.getTotal();
        }

        // Calcula os itens mais vendidos
        topItems = calculateTopItems(filteredTransactions);

        // Atualiza a tela
        setRevenue(totalRevenue);
        setTransactions(filteredTransactions.size());
        setTopItems(topItems);
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
     * Exports the current dashboard report overview to a text or CSV file.
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
     *
     * @param topItems list of map entries representing item names and sales quantity
     */
    public void setTopItems(List<Map.Entry<String, Integer>> topItems) {
        String[] rankColors = { "#6366F1", "#3B82F6", "#8B5CF6" }; // Indigo, Blue, Purple
        for (int i = 0; i < 3; i++) {
            if (i < topItems.size()) {
                Map.Entry<String, Integer> entry = topItems.get(i);
                topItemLabels[i].setText(String.format(
                    "<html><body style='font-family: Arial; font-size: 11px; margin: 3px;'>" +
                    "<span style='background-color: %s; color: white; padding: 2px 6px; font-weight: bold;'>#%d</span> " +
                    "<b style='color: #1E293B;'>%s</b> <font color='#64748B'>(%d sold)</font></body></html>",
                    rankColors[i], i + 1, entry.getKey(), entry.getValue()
                ));
            } else {
                topItemLabels[i].setText("<html><body style='font-family: Arial; color: #94A3B8; font-size: 11px;'>-</body></html>");
            }
        }
    }

    /**
     * Custom JPanel that draws a card with rounded corners and a soft drop shadow.
     */
    private static class ShadowCard extends JPanel {
        private final int cornerRadius;
        private final Color accentColor;
        private final int shadowWidth = 5;

        public ShadowCard(int cornerRadius, Color accentColor) {
            this.cornerRadius = cornerRadius;
            this.accentColor = accentColor;
            setOpaque(false);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            setLayout(new BorderLayout(10, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = shadowWidth;
            int y = shadowWidth;
            int w = getWidth() - 2 * shadowWidth;
            int h = getHeight() - 2 * shadowWidth;

            // Draw shadow layers
            for (int i = 0; i < shadowWidth; i++) {
                g2.setColor(new Color(0, 0, 0, 3 + i));
                g2.drawRoundRect(x - i, y - i, w + 2 * i, h + 2 * i, cornerRadius, cornerRadius);
            }

            // Draw card background
            g2.setColor(getBackground());
            g2.fillRoundRect(x, y, w, h, cornerRadius, cornerRadius);

            // Draw top accent line
            if (accentColor != null) {
                g2.setColor(accentColor);
                g2.fillRoundRect(x, y, w, 6, cornerRadius, cornerRadius);
                g2.fillRect(x, y + 3, w, 3);
            }

            g2.dispose();
        }
    }

    /**
     * Custom JButton with rounded corners, solid color, hand cursor, and hover effects.
     */
    private static class FlatButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color textCol;

        public FlatButton(String text, Color bg, Color hover, Color fg) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = hover;
            this.textCol = fg;

            setFont(new Font("Arial", Font.BOLD, 13));
            setForeground(fg);
            setBackground(bg);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(hoverBg);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBackground(normalBg);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            // If it's a light background button, draw a subtle border
            if (normalBg.getRed() > 220 && normalBg.getGreen() > 220 && normalBg.getBlue() > 220) {
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }

            // Draw button text manually for perfect centering
            g2.setColor(textCol);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getAscent();
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() - textHeight) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }
}
