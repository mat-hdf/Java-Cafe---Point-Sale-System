import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrderLogic implements ActionListener {
    
    private OrderGUI gui;
    private InventoryGUI inventoryGui;
    private double currentTotal = 0.0;

    public OrderLogic(OrderGUI gui, InventoryGUI inventoryGui) {
        this.gui = gui;
        this.inventoryGui = inventoryGui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String botaoClicado = e.getActionCommand(); 

        switch (botaoClicado) {
            case "Pie":
                adicionarItem("Pie", 15.50);
                break;
            case "Cake":
                adicionarItem("Cake", 12.00);
                break;
            case "Coffee":
                adicionarItem("Coffee", 5.00);
                break;
            case "Tea":
                adicionarItem("Tea", 4.00);
                break;
            case "Water":
                adicionarItem("Water", 2.00);
                break;
            case "Capuccino":
                adicionarItem("Capuccino", 6.00);
                break;
            case "Remove Item":
                removerItem();
                break;
            case "Cancel Order":
                cancelarPedido();
                break;
            case "Place Order":
                finalizarPedido();
                break;
        }
    }

    // Adiciona um item na tabela de pedidos
    private void adicionarItem(String nomeItem, double preco) {
        gui.getTableModel().addRow(new Object[]{1, nomeItem, String.format("%.2f", preco)});
        currentTotal += preco;
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));
    }

    // Remove o item selecionado da tabela
    private void removerItem() {
        int linhaSelecionada = gui.getOrderTable().getSelectedRow();
        
        if (linhaSelecionada != -1) { 
            String precoStr = (String) gui.getTableModel().getValueAt(linhaSelecionada, 2);
            double precoItem = Double.parseDouble(precoStr.replace(",", "."));
            
            currentTotal -= precoItem;
            gui.getOrderValueLabel().setText(String.format("%.2f", Math.max(0, currentTotal)));
            gui.getTableModel().removeRow(linhaSelecionada);
        }
    }

    // Cancela o pedido atual limpando a tabela
    private void cancelarPedido() {
        if (gui.getTableModel().getRowCount() == 0) return;
        
        int response = JOptionPane.showConfirmDialog(gui, "Are you sure you want to cancel the order?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            limparPedido();
        }
    }

    // Reseta o pedido
    private void limparPedido() {
        gui.getTableModel().setRowCount(0);
        currentTotal = 0.0;
        gui.getOrderValueLabel().setText("0.00");
    }

    // Finaliza o pedido realizando validacoes e atualizando estoque/vendas
    private void finalizarPedido() {
        DefaultTableModel model = gui.getTableModel();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(gui, "Your order is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1. Validacao de Estoque
            List<SalesPersistence.SaleItem> saleItems = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                int qty = (int) model.getValueAt(i, 0);
                String name = (String) model.getValueAt(i, 1);
                String priceStr = (String) model.getValueAt(i, 2);
                double price = Double.parseDouble(priceStr.replace(",", "."));

                int invRow = findInventoryRow(name);
                if (invRow == -1) {
                    throw new OutOfStockException("Product " + name + " not found in inventory.");
                }

                String stockStr = (String) inventoryGui.getTableModel().getValueAt(invRow, 2);
                int currentStock = Integer.parseInt(stockStr);

                if (currentStock < qty) {
                    throw new OutOfStockException("Insufficient stock for " + name + ".\nAvailable: " + currentStock + ", Requested: " + qty);
                }

                saleItems.add(new SalesPersistence.SaleItem(name, qty, price));
            }

            // 2. Calculo de Totais
            double subtotal = currentTotal;
            double tax = subtotal * 0.08; // Taxa de 8%
            double finalTotal = subtotal + tax;

            // 3. Processamento de Pagamento
            String paymentStr = JOptionPane.showInputDialog(gui, 
                String.format("Subtotal: $ %.2f\nTax (8%%): $ %.2f\nTotal: $ %.2f\n\nEnter payment amount:", subtotal, tax, finalTotal),
                "Payment", JOptionPane.QUESTION_MESSAGE);

            if (paymentStr == null) {
                return; // Cancelou a tela de pagamento
            }

            double payment;
            try {
                payment = Double.parseDouble(paymentStr.replace(",", "."));
            } catch (NumberFormatException ex) {
                throw new InvalidPaymentException("Payment amount must be a numeric value.");
            }

            if (payment < finalTotal) {
                throw new InvalidPaymentException(String.format("Insufficient payment.\nTotal: $ %.2f, Paid: $ %.2f", finalTotal, payment));
            }

            double change = payment - finalTotal;

            // 4. Deduzir Estoque no Inventario
            for (int i = 0; i < model.getRowCount(); i++) {
                int qty = (int) model.getValueAt(i, 0);
                String name = (String) model.getValueAt(i, 1);
                int invRow = findInventoryRow(name);
                String stockStr = (String) inventoryGui.getTableModel().getValueAt(invRow, 2);
                int currentStock = Integer.parseInt(stockStr);
                inventoryGui.getTableModel().setValueAt(String.valueOf(currentStock - qty), invRow, 2);
            }

            // 5. Salvar a Venda
            SalesPersistence.saveSale(saleItems);

            // 6. Exibir Recibo de Venda
            exibirRecibo(saleItems, subtotal, tax, finalTotal, payment, change);

            // 7. Limpar Pedido
            limparPedido();

        } catch (OutOfStockException ex) {
            JOptionPane.showMessageDialog(gui, ex.getMessage(), "Out of Stock Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPaymentException ex) {
            JOptionPane.showMessageDialog(gui, ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Procura a linha correspondente no painel de inventario
    private int findInventoryRow(String itemName) {
        for (int i = 0; i < inventoryGui.getTableModel().getRowCount(); i++) {
            String invName = (String) inventoryGui.getTableModel().getValueAt(i, 0);
            if (invName.equalsIgnoreCase(itemName) || 
                (itemName.equalsIgnoreCase("Capuccino") && invName.equalsIgnoreCase("Cappuccino")) ||
                (itemName.equalsIgnoreCase("Cappuccino") && invName.equalsIgnoreCase("Capuccino"))) {
                return i;
            }
        }
        return -1;
    }

    // Exibe o recibo formatado na tela
    private void exibirRecibo(List<SalesPersistence.SaleItem> items, double subtotal, double tax, double total, double paid, double change) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=========================================\n");
        receipt.append("               JAVA CAFE\n");
        receipt.append("=========================================\n");
        receipt.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        receipt.append("-----------------------------------------\n");
        for (SalesPersistence.SaleItem item : items) {
            receipt.append(String.format("%dx %-20s $ %7.2f\n", 
                item.getQuantity(), 
                item.getName(), 
                item.getPrice() * item.getQuantity()));
        }
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("Subtotal:                  $ %7.2f\n", subtotal));
        receipt.append(String.format("Tax (8%%):                  $ %7.2f\n", tax));
        receipt.append(String.format("Total:                     $ %7.2f\n", total));
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("Amount Paid:               $ %7.2f\n", paid));
        receipt.append(String.format("Change:                    $ %7.2f\n", change));
        receipt.append("=========================================\n");
        receipt.append("        Thank you for your visit!\n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(350, 400));
        
        JOptionPane.showMessageDialog(gui, scrollPane, "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }
}