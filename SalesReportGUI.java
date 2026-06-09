import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;
import java.util.Map;

/**
 * Graphical User Interface for the Sales Dashboard.
 * Displays key metrics like total revenue, transactions count, and the top-3 best selling items.
 */
public class SalesReportGUI extends JPanel {
    private JComboBox<String> periodComboBox;
    private JButton refreshButton;
    private JButton exportButton;

    private JLabel revenueValueLabel;
    private JLabel transactionsValueLabel;
    private JLabel[] topItemLabels;

    /**
     * Constructs a new SalesReportGUI dashboard with metric cards and action buttons.
     */
    public SalesReportGUI() {
        // Configura o layout principal com margens externas
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(243, 244, 246)); // Fundo cinza claro moderno

        // Painel superior de controle (Titulo e Acoes)
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Sales Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(31, 41, 55));
        controlPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        // Seletor de periodo
        periodComboBox = new JComboBox<>(new String[]{"Today", "Current Week", "Current Month"});
        periodComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        periodComboBox.setPreferredSize(new Dimension(150, 35));

        // Botao de atualizar
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setPreferredSize(new Dimension(100, 35));

        // Botao de exportar
        exportButton = new JButton("Export Report");
        exportButton.setFont(new Font("Arial", Font.BOLD, 14));
        exportButton.setPreferredSize(new Dimension(140, 35));

        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        periodLabel.setForeground(new Color(75, 85, 99));

        actionsPanel.add(periodLabel);
        actionsPanel.add(periodComboBox);
        actionsPanel.add(refreshButton);
        actionsPanel.add(exportButton);

        controlPanel.add(actionsPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.NORTH);

        // Painel central contendo os cards de metricas
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);

        // Card 1: Receita Total (Verde)
        JPanel revenueCard = createMetricCard("TOTAL REVENUE", new Color(16, 185, 129));
        revenueValueLabel = new JLabel("$ 0.00");
        revenueValueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        revenueValueLabel.setForeground(new Color(16, 185, 129));
        revenueValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        revenueCard.add(revenueValueLabel, BorderLayout.CENTER);

        // Card 2: Numero de Transacoes (Azul)
        JPanel transactionsCard = createMetricCard("TRANSACTIONS", new Color(59, 130, 246));
        transactionsValueLabel = new JLabel("0");
        transactionsValueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        transactionsValueLabel.setForeground(new Color(59, 130, 246));
        transactionsValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transactionsCard.add(transactionsValueLabel, BorderLayout.CENTER);

        // Card 3: Top-3 Itens (Roxo)
        JPanel topSellingCard = createMetricCard("TOP 3 BEST SELLERS", new Color(139, 92, 246));
        JPanel topItemsListPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topItemsListPanel.setOpaque(false);
        
        topItemLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            topItemLabels[i] = new JLabel("-");
            topItemLabels[i].setFont(new Font("Arial", Font.PLAIN, 15));
            topItemLabels[i].setForeground(new Color(55, 65, 81));
            topItemLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            topItemsListPanel.add(topItemLabels[i]);
        }
        topSellingCard.add(topItemsListPanel, BorderLayout.CENTER);

        cardsPanel.add(revenueCard);
        cardsPanel.add(transactionsCard);
        cardsPanel.add(topSellingCard);

        add(cardsPanel, BorderLayout.CENTER);
    }

    /**
     * Helper method to create a modern looking visual metric card.
     *
     * @param title       the title of the metric
     * @param accentColor the accent border color of the card
     * @return the styled JPanel card
     */
    private JPanel createMetricCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor), // Linha de destaque no topo
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            )
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(156, 163, 175));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);

        return card;
    }

    /**
     * Connects the controller logic to handle interaction events.
     *
     * @param logic the controller instance
     */
    public void setController(SalesReportLogic logic) {
        periodComboBox.addActionListener(logic);
        refreshButton.addActionListener(logic);
        exportButton.addActionListener(logic);
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
        for (int i = 0; i < 3; i++) {
            if (i < topItems.size()) {
                Map.Entry<String, Integer> entry = topItems.get(i);
                topItemLabels[i].setText(String.format("%d. %s (%d sold)", i + 1, entry.getKey(), entry.getValue()));
            } else {
                topItemLabels[i].setText("-");
            }
        }
    }
}
