import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InventoryGUI extends JPanel {
    
    // Declaração dos componentes
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScroll;
    
    private JPanel formPanel, actionPanel, mainBottomPanel;
    private JTextField nameField, priceField, stockField;
    private JButton addButton, updateButton, deleteButton, checkStockButton;

    public InventoryGUI() {
        // Configurações da janela
        setLayout(new BorderLayout(10,10));

        // Configuração da tabela do inventário
        String[] columnNames = {"Product Name", "Price ($)", "Stock Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(tableModel);
        tableScroll = new JScrollPane(inventoryTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        
        // Adicionando alguns dados de exemplo
        tableModel.addRow(new Object[]{"Pie", "4.50", "15"});
        tableModel.addRow(new Object[]{"Cake", "5.00", "8"});
        tableModel.addRow(new Object[]{"Coffee", "2.50", "50"});

        add(tableScroll, BorderLayout.CENTER);

        // Painel de Formulário para entrada de dados
        formPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(new JLabel("Price ($):"));
        formPanel.add(new JLabel("Stock Quantity:"));
        
        nameField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();
        
        formPanel.add(nameField);
        formPanel.add(priceField);
        formPanel.add(stockField);

        // Painel de Botões de Ação
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addButton = new JButton("Add Product"); // Botão para adicionar um novo produto ao inventário
        updateButton = new JButton("Update Stock"); // Botão para atualizar o estoque de um produto selecionado
        deleteButton = new JButton("Delete Product"); // Botão para deletar um produto selecionado
        checkStockButton = new JButton("Check Low Stock");  // Botão para verificar produtos com estoque baixo
        
        actionPanel.add(addButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(checkStockButton);

        // Agrupando o formulário e os botões na parte inferior (SOUTH)
        mainBottomPanel = new JPanel(new BorderLayout());
        mainBottomPanel.add(formPanel, BorderLayout.CENTER);
        mainBottomPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainBottomPanel, BorderLayout.SOUTH);
    }
}
