import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class InventoryLogic implements ActionListener {
    
    private InventoryGUI gui;

    public InventoryLogic(InventoryGUI gui) {
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        switch (actionCommand) {
            case "Add Product":
                addProduct();
                break;
            case "Check Low Stock":
                checkLowStock();
                break;
        }
    }

    // Adds the product using the data from the bottom form
    private void addProduct() {
        String name = gui.getNameField().getText().trim();
        String priceStr = gui.getPriceField().getText().trim();
        String stockStr = gui.getStockField().getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr.replace(",", "."));
            int stock = Integer.parseInt(stockStr);

            // Lock to prevent negative values
            if (price < 0 || stock < 0) {
                JOptionPane.showMessageDialog(gui, "Price and Stock must be positive numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Adds the row using Locale.US to ensure the correct "X.XX" format and the empty space for the button
            gui.getTableModel().addRow(new Object[]{name, String.format(java.util.Locale.US, "%.2f", price), String.valueOf(stock), ""});

            gui.getNameField().setText("");
            gui.getPriceField().setText("");
            gui.getStockField().setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(gui, "Price must be a decimal and Stock must be an integer.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Deletes the product (called directly by mouse click on the red button in the GUI)
    public void deleteProduct(int row) {
        int response = JOptionPane.showConfirmDialog(gui, "Are you sure you want to delete this product?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            gui.getTableModel().removeRow(row);
        }
    }

    // Checks for low stock by scanning the table
    private void checkLowStock() {
        DefaultTableModel model = gui.getTableModel();
        boolean hasLowStock = false;
        int threshold = (int) gui.getThresholdSpinner().getValue(); 
        StringBuilder lowStockItems = new StringBuilder("Items with Low Stock (Below " + threshold + "):\n\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                String name = (String) model.getValueAt(i, 0);
                
                // trim() removes whitespace in case the user types with space during direct editing
                String stockRaw = (String) model.getValueAt(i, 2);
                int stock = Integer.parseInt(stockRaw.trim());

                if (stock < threshold) {
                    lowStockItems.append("- ").append(name).append(" (Current Stock: ").append(stock).append(")\n");
                    hasLowStock = true;
                }
            } catch (Exception ex) {
                // Prevents crash in case the user entered text in the stock column
                System.out.println("Error reading stock at row " + i + ". Ensure it is an integer number.");
            }
        }

        if (hasLowStock) {
            JOptionPane.showMessageDialog(gui, lowStockItems.toString(), "Stock Alert", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(gui, "All items have sufficient stock levels.", "Stock Status", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method called by OrderLogic to automatically decrease stock on sales
    public void decreaseStock(String productName, int quantitySold) {
        DefaultTableModel model = gui.getTableModel();
        int threshold = (int) gui.getThresholdSpinner().getValue();

        for (int i = 0; i < model.getRowCount(); i++) {
            String currentName = (String) model.getValueAt(i, 0);
            
            if (currentName != null && currentName.equalsIgnoreCase(productName)) {
                try {
                    String stockRaw = (String) model.getValueAt(i, 2);
                    int currentStock = Integer.parseInt(stockRaw.trim());
                    int newStock = currentStock - quantitySold;
                    
                    if (newStock < 0) newStock = 0;

                    model.setValueAt(String.valueOf(newStock), i, 2);

                    if (newStock < threshold) {
                        JOptionPane.showMessageDialog(gui, 
                            "Auto-Alert: Stock for '" + productName + "' dropped to " + newStock + "!\n(Below threshold of " + threshold + ")", 
                            "Low Stock Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Error decreasing stock for item " + productName);
                }
                break; // Product found and updated, exits the loop
            }
        }
    }
}