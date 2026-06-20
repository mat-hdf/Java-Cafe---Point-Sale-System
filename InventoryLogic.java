import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class InventoryLogic implements ActionListener {
    
    private InventoryGUI gui;
    private OrderGUI orderGui;
    private OrderLogic orderLogic;
    private static final String FILE_PATH = "inventory.csv";

    public InventoryLogic(InventoryGUI gui) {
        this.gui = gui;
        loadInventory();
        this.gui.getTableModel().addTableModelListener(e -> saveInventory());
    }

    /**
     * Binds the ordering UI system to allow dynamic automatic UI re-renders on data modification
     */
    public void bindOrderSystem(OrderGUI orderGui, OrderLogic orderLogic) {
        this.orderGui = orderGui;
        this.orderLogic = orderLogic;
        
        // Initial setup load of the order menu grid
        this.orderGui.refreshMenu(this.gui.getTableModel(), this.orderLogic);
        
        // Re-triggers menu builds anytime changes are detected on the inventory rows
        this.gui.getTableModel().addTableModelListener(e -> {
            if (this.orderGui != null && this.orderLogic != null) {
                this.orderGui.refreshMenu(this.gui.getTableModel(), this.orderLogic);
            }
        });
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
        String imagePath = gui.getImagePathField().getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (imagePath.isEmpty()) {
            imagePath = "imgs/default.jpg"; // Automatic placeholder fallback if none picked
        }

        try {
            double price = Double.parseDouble(priceStr.replace(",", "."));
            int stock = Integer.parseInt(stockStr);

            if (price < 0 || stock < 0) {
                JOptionPane.showMessageDialog(gui, "Price and Stock must be positive numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Appends the row capturing the assigned file path to column 3
            gui.getTableModel().addRow(new Object[]{name, String.format(java.util.Locale.US, "%.2f", price), String.valueOf(stock), imagePath, ""});

            gui.getNameField().setText("");
            gui.getPriceField().setText("");
            gui.getStockField().setText("");
            gui.getImagePathField().setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(gui, "Price must be a decimal and Stock must be an integer.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteProduct(int row) {
        int response = JOptionPane.showConfirmDialog(gui, "Are you sure you want to delete this product?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            gui.getTableModel().removeRow(row);
        }
    }

    private void checkLowStock() {
        DefaultTableModel model = gui.getTableModel();
        boolean hasLowStock = false;
        int threshold = (int) gui.getThresholdSpinner().getValue(); 
        StringBuilder lowStockItems = new StringBuilder("Items with Low Stock (Below " + threshold + "):\n\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                String name = (String) model.getValueAt(i, 0);
                String stockRaw = (String) model.getValueAt(i, 2);
                int stock = Integer.parseInt(stockRaw.trim());

                if (stock < threshold) {
                    lowStockItems.append("- ").append(name).append(" (Current Stock: ").append(stock).append(")\n");
                    hasLowStock = true;
                }
            } catch (Exception ex) {
                System.out.println("Error reading stock at row " + i);
            }
        }

        if (hasLowStock) {
            JOptionPane.showMessageDialog(gui, lowStockItems.toString(), "Stock Alert", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(gui, "All items have sufficient stock levels.", "Stock Status", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void decreaseStock(String productName, int quantitySold) {
        DefaultTableModel model = gui.getTableModel();
        int threshold = (int) gui.getThresholdSpinner().getValue();

        for (int i = 0; i < model.getRowCount(); i++) {
            String currentName = (String) model.getValueAt(i, 0);
            if (isSameProduct(currentName, productName)) {
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
                break;
            }
        }
    }
    
    /**
     * Looks up prices live inside data entries matching key criteria string
     */
    public double getPrice(String productName) {
        DefaultTableModel model = gui.getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String currentName = (String) model.getValueAt(i, 0);
            if (isSameProduct(currentName, productName)) {
                try {
                    String priceRaw = (String) model.getValueAt(i, 1);
                    return Double.parseDouble(priceRaw.replace(",", "."));
                } catch (NumberFormatException ex) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }

    public void loadInventory() {
        DefaultTableModel model = gui.getTableModel();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            saveInventory();
            return;
        }

        model.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                String name = parts[0];
                String price = parts[1];
                String stock = parts[2];
                String imgPath = parts.length > 3 ? parts[3] : "imgs/default.jpg";
                model.addRow(new Object[]{name, price, stock, imgPath, ""});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveInventory() {
        File file = new File(FILE_PATH);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("product_name,price,stock_quantity,image_path");
            DefaultTableModel model = gui.getTableModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String name = (String) model.getValueAt(i, 0);
                String price = (String) model.getValueAt(i, 1);
                String stock = (String) model.getValueAt(i, 2);
                String imgPath = (String) model.getValueAt(i, 3);
                pw.printf("%s,%s,%s,%s\n", name, price, stock, imgPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getStock(String productName) {
        DefaultTableModel model = gui.getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String currentName = (String) model.getValueAt(i, 0);
            if (isSameProduct(currentName, productName)) {
                try {
                    String stockRaw = (String) model.getValueAt(i, 2);
                    return Integer.parseInt(stockRaw.trim());
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private boolean isSameProduct(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        String n1 = name1.trim().toLowerCase();
        String n2 = name2.trim().toLowerCase();
        if (n1.equals("capuccino") || n1.equals("cappuccino")) {
            return n2.equals("capuccino") || n2.equals("cappuccino");
        }
        return n1.equals(n2);
    }
}