import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            // Cria a janela principal única do sistema
            JFrame mainFrame = new JFrame("Java Cafe - Management System");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(800, 600);

            // Cria o componente das abas
            JTabbedPane tabbedPane = new JTabbedPane();

            // Cria as telas de pedidos e inventário
            OrderGUI orderScreen = new OrderGUI();
            InventoryGUI inventoryScreen = new InventoryGUI();
            
            // Cria um painel temporário para a tela de relatórios de vendas
            JPanel salesReportScreen = new JPanel();

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