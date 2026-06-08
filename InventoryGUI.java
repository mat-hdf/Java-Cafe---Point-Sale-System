import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener; 
import javax.swing.table.DefaultTableModel;    

public class InventoryGUI extends JPanel {
    
    // Declaração dos componentes
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScroll;
    
    private JPanel formPanel, actionPanel, mainBottomPanel;
    private JTextField nameField, priceField, stockField;
    private JButton addButton, updateButton, deleteButton, checkStockButton;
    
    private JSpinner thresholdSpinner; 
    private JButton clearSelectionButton;

    public InventoryGUI() {
        // Configurações da janela
        setLayout(new BorderLayout(10,10));

        // Configuração da tabela do inventário
        String[] columnNames = {"Product Name", "Price ($)", "Stock Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Isso impede o usuário de editar as células diretamente na tabela
            }
        };
        inventoryTable = new JTable(tableModel);
        tableScroll = new JScrollPane(inventoryTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        
        // Adicionando alguns dados de exemplo
        tableModel.addRow(new Object[]{"Pie", "4.50", "10"});
        tableModel.addRow(new Object[]{"Cake", "5.00", "8"});
        tableModel.addRow(new Object[]{"Coffee", "2.50", "40"});
        tableModel.addRow(new Object[]{"Tea", "2.50", "25"});
        tableModel.addRow(new Object[]{"Water", "1.00", "50"});
        tableModel.addRow(new Object[]{"Cappuccino", "2.50", "25"});

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
        addButton = new JButton("Add Product");                 // Botão para adicionar um novo produto ao inventário
        clearSelectionButton = new JButton("Clear Selection");  // Botão para limpar a seleção da tabela e os campos de texto
        updateButton = new JButton("Update Product");           // Botão para atualizar o estoque de um produto selecionado
        deleteButton = new JButton("Delete Product");           // Botão para deletar um produto selecionado
        checkStockButton = new JButton("Check Low Stock");      // Botão para verificar produtos com estoque baixo
        
        // Configuração do Spinner e ocultação de Botões
        SpinnerModel spinnerModel = new SpinnerNumberModel(10, 1, 50, 1);
        thresholdSpinner = new JSpinner(spinnerModel);
        JPanel thresholdContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        thresholdContainer.add(new JLabel("Low Stock Threshold:"));
        thresholdContainer.add(thresholdSpinner);

        // Esses botões só aparecem quando um produto é selecionado na tabela
        updateButton.setVisible(false); 
        deleteButton.setVisible(false);
        clearSelectionButton.setVisible(false);

        inventoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { 
                    boolean rowSelected = inventoryTable.getSelectedRow() != -1;
                    
                    if (rowSelected) {
                        // Puxa os dados apenas se uma linha for selecionada
                        int row = inventoryTable.getSelectedRow();
                        nameField.setText((String) tableModel.getValueAt(row, 0));
                        priceField.setText((String) tableModel.getValueAt(row, 1));
                        stockField.setText((String) tableModel.getValueAt(row, 2));
                    }

                    // Atualiza a visibilidade dos botões com base na seleção
                    addButton.setVisible(!rowSelected); 
                    updateButton.setVisible(rowSelected);
                    deleteButton.setVisible(rowSelected);
                    clearSelectionButton.setVisible(rowSelected); 
                    
                    actionPanel.revalidate();
                    actionPanel.repaint();
                }
            }
        });

        // Ação que limpa a seleção da tabela e os campos de texto
        clearSelectionButton.addActionListener(e -> {
            inventoryTable.clearSelection();
            nameField.setText("");
            priceField.setText("");
            stockField.setText("");
        });

        // Adiciona os botões ao painel de ação
        actionPanel.add(addButton);
        actionPanel.add(clearSelectionButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(checkStockButton);
        

        // Agrupando o formulário e os botões na parte inferior (SOUTH)
        mainBottomPanel = new JPanel(new BorderLayout());
        mainBottomPanel.add(thresholdContainer, BorderLayout.NORTH);
        mainBottomPanel.add(formPanel, BorderLayout.CENTER);
        mainBottomPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainBottomPanel, BorderLayout.SOUTH);
    }

    // Método para conectar os botões à classe de lógica
    public void setController(InventoryLogic logic) {
        addButton.addActionListener(logic);
        clearSelectionButton.addActionListener(logic);
        updateButton.addActionListener(logic);
        deleteButton.addActionListener(logic);
        checkStockButton.addActionListener(logic);
    }

    // Métodos de acesso para a classe lógica interagir com a GUI
    public JTable getInventoryTable() { return inventoryTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getNameField() { return nameField; }
    public JTextField getPriceField() { return priceField; }
    public JTextField getStockField() { return stockField; }
    public JSpinner getThresholdSpinner() { return thresholdSpinner; }
}