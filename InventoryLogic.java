import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class InventoryLogic implements ActionListener {
    // GUI para acessar os componentes e atualizar a interface
    private InventoryGUI gui;

    public InventoryLogic(InventoryGUI gui) {
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String botaoClicado = e.getActionCommand();

        // Switch para determinar qual ação executar
        switch (botaoClicado) {
            case "Add Product":
                addProduct();
                break;
            case "Update Product": 
                updateProduct();   
                break;
            case "Delete Product":
                deleteProduct();
                break;
            case "Check Low Stock":
                checkLowStock();
                break;
        }
    }

    // Métodos de Ação

    private void addProduct() {
        String name = gui.getNameField().getText();
        String priceStr = gui.getPriceField().getText();
        String stockStr = gui.getStockField().getText();

        // Validação simples de campos vazios
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Tenta converter os textos para números (Tratamento de exceção)
            double price = Double.parseDouble(priceStr.replace(",", "."));
            int stock = Integer.parseInt(stockStr);

            // Adiciona na tabela
            DefaultTableModel model = gui.getTableModel();
            model.addRow(new Object[]{name, String.format("%.2f", price), String.valueOf(stock)});

            // Limpa os campos após adicionar
            gui.getNameField().setText("");
            gui.getPriceField().setText("");
            gui.getStockField().setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(gui, "Price must be a decimal and Stock must be an integer.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct() {
        int selectedRow = gui.getInventoryTable().getSelectedRow();
        
        if (selectedRow == -1) {
            return; // Medida de segurança, embora o botão esteja oculto se não houver seleção
        }

        // Pega os valores dos campos de texto para atualizar a linha selecionada
        String name = gui.getNameField().getText();
        String priceStr = gui.getPriceField().getText();
        String stockStr = gui.getStockField().getText();

        // Validação simples de campos vazios
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "Please fill in all fields to update.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr.replace(",", "."));
            int stock = Integer.parseInt(stockStr);

            // Atualiza todas as colunas da linha selecionada
            gui.getTableModel().setValueAt(name, selectedRow, 0);
            gui.getTableModel().setValueAt(String.format("%.2f", price), selectedRow, 1);
            gui.getTableModel().setValueAt(String.valueOf(stock), selectedRow, 2);
            
            // Remove a seleção da tabela para resetar a interface
            gui.getInventoryTable().clearSelection();
            
            JOptionPane.showMessageDialog(gui, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            // Validação de formato para preço e estoque
            JOptionPane.showMessageDialog(gui, "Price must be a decimal and Stock must be an integer.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int selectedRow = gui.getInventoryTable().getSelectedRow();
        
        if (selectedRow != -1) {
            // Confirmação antes de deletar
            int response = JOptionPane.showConfirmDialog(gui, "Are you sure you want to delete this product?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                gui.getTableModel().removeRow(selectedRow);
            }
        } else {
            JOptionPane.showMessageDialog(gui, "Please select a product to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkLowStock() {
        DefaultTableModel model = gui.getTableModel();
        boolean hasLowStock = false;
        
        // Captura o valor diretamente do JSpinner na GUI
        int threshold = (int) gui.getThresholdSpinner().getValue(); 
        
        // StringBuilder para construir a mensagem de itens com baixo estoque
        StringBuilder lowStockItems = new StringBuilder("Items with Low Stock (Below " + threshold + "):\n\n");

        // Itera sobre as linhas da tabela para verificar o estoque de cada item
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = (String) model.getValueAt(i, 0);
            int stock = Integer.parseInt((String) model.getValueAt(i, 2));

            // Se estiver abaixo do limite, adiciona à mensagem
            if (stock < threshold) {
                lowStockItems.append("- ").append(name).append(" (Current Stock: ").append(stock).append(")\n");
                hasLowStock = true;
            }
        }

        // Exibe a mensagem de itens com baixo estoque ou uma mensagem informando que tudo está ok
        if (hasLowStock) {
            JOptionPane.showMessageDialog(gui, lowStockItems.toString(), "Stock Status", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(gui, "All items have sufficient stock levels.", "Stock Status", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}