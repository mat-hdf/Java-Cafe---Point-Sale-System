import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            // Cria a janela principal única do sistema
            JFrame mainFrame = new JFrame("Java Cafe - Management System");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1000, 800);

            // Cria o componente das abas
            JTabbedPane tabbedPane = new JTabbedPane();

            // Cria as telas de pedidos e inventário
            InventoryGUI inventoryScreen = new InventoryGUI();
            InventoryLogic invLogic = new InventoryLogic(inventoryScreen);
            inventoryScreen.setController(invLogic);

            OrderGUI orderScreen = new OrderGUI();
            OrderLogic logic = new OrderLogic(orderScreen, inventoryScreen);
            orderScreen.setController(logic);
            
            // Cria as abas de relatórios de vendas
            SalesReportGUI salesReportScreen = new SalesReportGUI();
            SalesReportLogic salesReportLogic = new SalesReportLogic(salesReportScreen);
            salesReportScreen.setController(salesReportLogic);

            // Atualiza os relatórios automaticamente ao alternar para a aba correspondente
            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedIndex() == 2) {
                    salesReportLogic.refresh();
                }
            });

            // Adiciona cada painel como uma aba diferente
            tabbedPane.addTab("Orders", orderScreen);
            tabbedPane.addTab("Inventory", inventoryScreen);
            tabbedPane.addTab("Sales Reports", salesReportScreen);

            // Adiciona o painel de abas à janela principal
            mainFrame.add(tabbedPane);
            
            // Centraliza e exibe a janela
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }
}