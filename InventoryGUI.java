import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class InventoryGUI extends JPanel {
    
    // Component declarations
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScroll;
    
    private JPanel formPanel, actionPanel, mainBottomPanel;
    private JTextField nameField, priceField, stockField;
    private JButton addButton, checkStockButton, clearSelectionButton;
    
    private JSpinner thresholdSpinner; 

    public InventoryGUI() {
        // Main panel settings
        setLayout(new BorderLayout(10,10));

        // 1. Table Configuration (Data columns + 1 Delete Column)
        String[] columnNames = {"Product Name", "Price ($)", "Stock Quantity", "Delete"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 3; // Blocks only the Delete button column (3)
            }

            // Intercepts and validates any direct editing done on the table
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                String input = aValue.toString().trim();
                
                // Validation for the Price column (1)
                if (column == 1) {
                    try {
                        double price = Double.parseDouble(input.replace(",", "."));
                        if (price < 0) {
                            JOptionPane.showMessageDialog(null, "Price must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                            return; // Aborts the editing
                        }
                        // Locale.US forces Java to use a dot instead of a comma
                        super.setValueAt(String.format(java.util.Locale.US, "%.2f", price), row, column);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid price format. Please enter a valid number.", "Format Error", JOptionPane.ERROR_MESSAGE);
                    }
                } 
                // Validation for the Stock column (2)
                else if (column == 2) {
                    try {
                        int stock = Integer.parseInt(input);
                        if (stock < 0) {
                            JOptionPane.showMessageDialog(null, "Stock must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                            return; // Aborts the editing
                        }
                        super.setValueAt(String.valueOf(stock), row, column);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid stock format. Please enter a whole number.", "Format Error", JOptionPane.ERROR_MESSAGE);
                    }
                } 
                // Validation for the Name column (0)
                else {
                    if (input.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Product name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return; // Aborts the editing
                    }
                    super.setValueAt(input, row, column);
                }
            }
        };
        
        inventoryTable = new JTable(tableModel);
        
        // Makes the font of the items in the table larger and crisper
        inventoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inventoryTable.setRowHeight(30);
        
        // Renders column 3 as a button with a red "X" (\u2716)
        inventoryTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("\u2716 Del", new Color(220, 20, 60)));  
        inventoryTable.getColumnModel().getColumn(3).setMaxWidth(80);

        tableScroll = new JScrollPane(inventoryTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        
        // Adding some sample data (The last empty string "" reserves space for the button)
        tableModel.addRow(new Object[]{"Pie", "4.50", "10", ""});
        tableModel.addRow(new Object[]{"Cake", "5.00", "8", ""});
        tableModel.addRow(new Object[]{"Coffee", "2.50", "40", ""});
        tableModel.addRow(new Object[]{"Tea", "2.50", "25", ""});
        tableModel.addRow(new Object[]{"Water", "1.00", "50", ""});
        tableModel.addRow(new Object[]{"Cappuccino", "2.50", "25", ""});

        add(tableScroll, BorderLayout.CENTER);

        // Form Panel for data entry (Exclusive for ADDING new products)
        formPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Product"));
        
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(new JLabel("Price ($):"));
        formPanel.add(new JLabel("Stock Quantity:"));
        
        nameField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();
        
        formPanel.add(nameField);
        formPanel.add(priceField);
        formPanel.add(stockField);

        // Action Buttons Panel
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addButton = new JButton("Add Product"); // Button to add a new product to the inventory
        clearSelectionButton = new JButton("Clear Selection"); // Button to clear the fields
        checkStockButton = new JButton("Check Low Stock");  // Button to check for products with low stock

        // Low Stock Threshold Configuration
        SpinnerModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
        thresholdSpinner = new JSpinner(spinnerModel);
        JPanel thresholdContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdContainer.add(new JLabel("Low Stock Threshold:"));
        thresholdContainer.add(thresholdSpinner);

        actionPanel.add(addButton);
        actionPanel.add(clearSelectionButton);
        actionPanel.add(checkStockButton);

        // Action to clear the form fields
        clearSelectionButton.addActionListener(e -> {
            inventoryTable.clearSelection();
            nameField.setText("");
            priceField.setText("");
            stockField.setText("");
        });

        // Grouping the form and buttons at the bottom (SOUTH)
        mainBottomPanel = new JPanel(new BorderLayout());
        mainBottomPanel.add(thresholdContainer, BorderLayout.NORTH); 
        mainBottomPanel.add(formPanel, BorderLayout.CENTER);
        mainBottomPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainBottomPanel, BorderLayout.SOUTH);
    }

    // Method to connect the buttons to the logic class
    public void setController(InventoryLogic logic) {
        addButton.addActionListener(logic);
        checkStockButton.addActionListener(logic);

        // Listener that detects mouse clicks directly on the Delete column
        inventoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = inventoryTable.rowAtPoint(e.getPoint());
                int col = inventoryTable.columnAtPoint(e.getPoint());
                
                // If a valid row and exactly column 3 (Delete) was clicked
                if (row >= 0 && col == 3) { 
                    logic.deleteProduct(row);
                }
            }
        });
    }

    // --- Access methods for the logic class to interact with the GUI ---
    public JTable getInventoryTable() { return inventoryTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getNameField() { return nameField; }
    public JTextField getPriceField() { return priceField; }
    public JTextField getStockField() { return stockField; }
    public JSpinner getThresholdSpinner() { return thresholdSpinner; } 

    // --- Inner class that draws the button in the cell ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text, Color bgColor) {
            setText(text);
            setBackground(bgColor);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setOpaque(true);
            
            // SansSerif font in Bold size 14 ensures the "X" icon is crisp
            setFont(new Font("SansSerif", Font.BOLD, 14));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
}