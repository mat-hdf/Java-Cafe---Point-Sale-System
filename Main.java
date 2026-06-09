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

            // Cria a tela de pedidos
            OrderGUI orderScreen = new OrderGUI();
            OrderLogic logic = new OrderLogic(orderScreen);
            orderScreen.setController(logic);

            // Cria a tela de inventário
            InventoryGUI inventoryScreen = new InventoryGUI();
            InventoryLogic invLogic = new InventoryLogic(inventoryScreen);
            inventoryScreen.setController(invLogic);
            
            // Cria a tela de relatórios de vendas
            ReportGUI reportScreen = new ReportGUI();

            // Adiciona cada painel como uma aba diferente
            tabbedPane.addTab("Orders", orderScreen);
            tabbedPane.addTab("Inventory", inventoryScreen);
            tabbedPane.addTab("Sales Reports", reportScreen);

            // Adiciona o painel de abas à janela principal
            mainFrame.add(tabbedPane);
            
            // Centraliza e exibe a janela
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }
}