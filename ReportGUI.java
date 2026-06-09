import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReportGUI extends JPanel {

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> timeFilterCombo;
    private JLabel totalRevenueLabel;
    private JLabel totalOrdersLabel;
    private JLabel topItemsLabel;
    
    // Lista que atua como nosso "Banco de Dados" em memória
    private List<SaleRecord> salesHistory;

    public ReportGUI() {
        salesHistory = new ArrayList<>();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Painel Superior: Filtro de Tempo
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Select Time Period: "));
        String[] periods = {"Today", "Current Week", "Current Month"};
        timeFilterCombo = new JComboBox<>(periods);
        
        // Adiciona a ação para recalcular tudo quando o filtro mudar
        timeFilterCombo.addActionListener(e -> updateReportView());
        filterPanel.add(timeFilterCombo);
        add(filterPanel, BorderLayout.NORTH);

        // 2. Configuração da Tabela de Relatórios
        String[] columnNames = {"Date/Time", "Items Sold", "Total ($)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        reportTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(reportTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Sales History"));
        
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(350);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(80);

        add(tableScroll, BorderLayout.CENTER);

        // 3. Painel de Resumo (Revenue, Transactions, Top 3)
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Performance Summary"));
        summaryPanel.setBackground(new Color(240, 248, 255)); 

        totalOrdersLabel = new JLabel("Total Transactions: 0");
        totalOrdersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        totalRevenueLabel = new JLabel("Total Revenue: $0.00");
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalRevenueLabel.setForeground(new Color(0, 100, 0));
        
        topItemsLabel = new JLabel("Top 3 Items: N/A");
        topItemsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topItemsLabel.setForeground(Color.BLUE);

        summaryPanel.add(totalOrdersLabel);
        summaryPanel.add(totalRevenueLabel);
        summaryPanel.add(topItemsLabel);

        add(summaryPanel, BorderLayout.SOUTH);
    }

    /**
     * Recebe a venda finalizada da OrderLogic e guarda no histórico
     */
    public void addSaleRecord(List<String> itemsSold, double orderTotal) {
        SaleRecord novaVenda = new SaleRecord(LocalDateTime.now(), itemsSold, orderTotal);
        salesHistory.add(novaVenda);
        updateReportView(); // Atualiza a tela automaticamente
    }

    /**
     * Lógica principal: Filtra a lista pelo tempo selecionado e calcula os totais e o Top 3
     */
    private void updateReportView() {
        tableModel.setRowCount(0); // Limpa a tabela
        
        String selectedFilter = (String) timeFilterCombo.getSelectedItem();
        LocalDate today = LocalDate.now();
        
        double currentRevenue = 0.0;
        int currentTransactions = 0;
        Map<String, Integer> itemCounts = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (SaleRecord sale : salesHistory) {
            LocalDate saleDate = sale.date.toLocalDate();
            boolean includeSale = false;

            // Filtros de tempo
            if (selectedFilter.equals("Today")) {
                includeSale = saleDate.isEqual(today);
            } else if (selectedFilter.equals("Current Week")) {
                // Checa se a data da venda não é antes da última segunda-feira
                LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
                includeSale = !saleDate.isBefore(startOfWeek) && !saleDate.isAfter(today);
            } else if (selectedFilter.equals("Current Month")) {
                includeSale = saleDate.getMonth() == today.getMonth() && saleDate.getYear() == today.getYear();
            }

            // Se a venda passou no filtro, processa os dados dela
            if (includeSale) {
                // 1. Adiciona na tabela
                String itemsString = String.join(", ", sale.items);
                tableModel.addRow(new Object[]{sale.date.format(formatter), itemsString, String.format("%.2f", sale.total)});
                
                // 2. Soma transações e dinheiro
                currentTransactions++;
                currentRevenue += sale.total;
                
                // 3. Conta os itens para o Top 3
                for (String item : sale.items) {
                    itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
                }
            }
        }

        // Atualiza as Labels de Resumo
        totalOrdersLabel.setText("Total Transactions: " + currentTransactions);
        totalRevenueLabel.setText("Total Revenue: $" + String.format("%.2f", currentRevenue));
        
        // Calcula e formata o Top 3
        topItemsLabel.setText(calculateTop3(itemCounts));
    }

    /**
     * Ordena os itens mais vendidos e retorna uma String formatada
     */
    private String calculateTop3(Map<String, Integer> counts) {
        if (counts.isEmpty()) return "Top 3 Items: No sales in this period";

        // Converte o Map para uma Lista para podermos ordenar
        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        
        // Ordena do maior pro menor
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder top3Text = new StringBuilder("Top 3 Items: ");
        int limit = Math.min(3, list.size()); // Pega até 3
        
        for (int i = 0; i < limit; i++) {
            top3Text.append(i + 1).append(". ").append(list.get(i).getKey());
            top3Text.append(" (").append(list.get(i).getValue()).append("x)");
            if (i < limit - 1) top3Text.append(" | ");
        }

        return top3Text.toString();
    }

    // --- CLASSE INTERNA PARA GUARDAR OS DADOS DA VENDA ---
    private class SaleRecord {
        LocalDateTime date;
        List<String> items;
        double total;

        public SaleRecord(LocalDateTime date, List<String> items, double total) {
            this.date = date;
            this.items = items;
            this.total = total;
        }
    }
}