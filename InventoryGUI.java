import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class InventoryGUI extends JPanel {
    
    // Component declarations
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScroll;
    
    private JPanel formPanel, actionPanel, mainBottomPanel;
    private JTextField nameField, priceField, stockField, imagePathField;
    private JButton addButton, checkStockButton, clearSelectionButton, browseImageButton;
    
    private JSpinner thresholdSpinner; 

    public InventoryGUI() {
        // Main panel settings
        setLayout(new BorderLayout(15, 15));
        setBackground(CafeTheme.OFF_WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Table Configuration (Data columns + 1 Image Path Column + 1 Delete Column)
        String[] columnNames = {"Product Name", "Price ($)", "Stock Quantity", "Image Path", "Delete"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 4; // Blocks only the Delete button column (4)
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
                else if (column == 0) {
                    if (input.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Product name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return; // Aborts the editing
                    }
                    super.setValueAt(input, row, column);
                }
                // Validation for direct Image Path edits (3)
                else {
                    super.setValueAt(input, row, column);
                }
            }
        };
        
        inventoryTable = new JTable(tableModel);
        CafeTheme.styleTable(inventoryTable);
        
        // Renders column 4 as a beautiful, centered terracotta button
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());  
        inventoryTable.getColumnModel().getColumn(4).setMaxWidth(80);

        // Change mouse cursor to hand when hovering over the delete column (col 4)
        inventoryTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int col = inventoryTable.columnAtPoint(e.getPoint());
                if (col == 4) {
                    inventoryTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    inventoryTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        tableScroll = new JScrollPane(inventoryTable);
        CafeTheme.styleScrollPane(tableScroll);
        tableScroll.setBorder(CafeTheme.createCafeTitledBorder("Current Inventory"));
        
        // Adding some sample data (The last empty string "" reserves space for the button)
        tableModel.addRow(new Object[]{"Pie", "4.50", "10", "imgs/pie.jpg", ""});
        tableModel.addRow(new Object[]{"Cake", "5.00", "8", "imgs/cake.jpeg", ""});
        tableModel.addRow(new Object[]{"Coffee", "2.50", "40", "imgs/coffee.JPG", ""});
        tableModel.addRow(new Object[]{"Tea", "2.50", "25", "imgs/tea.jpg", ""});
        tableModel.addRow(new Object[]{"Water", "1.00", "50", "imgs/commercial-pet-bottle.jpg", ""});
        tableModel.addRow(new Object[]{"Cappuccino", "2.50", "25", "imgs/capuccino.jpg", ""});

        add(tableScroll, BorderLayout.CENTER);

        // Form Panel for data entry (Exclusive for ADDING new products)
        formPanel = new JPanel(new GridLayout(2, 4, 12, 6));
        formPanel.setBackground(CafeTheme.OFF_WHITE);
        formPanel.setBorder(CafeTheme.createCafeTitledBorder("Add New Product"));
        
        JLabel nameLbl = new JLabel("Product Name:");
        nameLbl.setFont(CafeTheme.BOLD_FONT);
        nameLbl.setForeground(CafeTheme.DARK_ROAST);
        
        JLabel priceLbl = new JLabel("Price ($):");
        priceLbl.setFont(CafeTheme.BOLD_FONT);
        priceLbl.setForeground(CafeTheme.DARK_ROAST);
        
        JLabel stockLbl = new JLabel("Stock Quantity:");
        stockLbl.setFont(CafeTheme.BOLD_FONT);
        stockLbl.setForeground(CafeTheme.DARK_ROAST);
        
        JLabel imgLbl = new JLabel("Product Image:");
        imgLbl.setFont(CafeTheme.BOLD_FONT);
        imgLbl.setForeground(CafeTheme.DARK_ROAST);

        formPanel.add(nameLbl);
        formPanel.add(priceLbl);
        formPanel.add(stockLbl);
        formPanel.add(imgLbl);
        
        nameField = new JTextField();
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CafeTheme.CREAM_DARK, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        priceField = new JTextField();
        priceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CafeTheme.CREAM_DARK, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        stockField = new JTextField();
        stockField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CafeTheme.CREAM_DARK, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        // Setup image path field as a hidden model component
        imagePathField = new JTextField();
        
        browseImageButton = new CafeTheme.CafeButton("Choose Image", CafeTheme.CafeButton.Variant.SECONDARY);
        browseImageButton.setToolTipText("Select a valid image file (.jpg, .jpeg, .png, .gif)");
        
        // DocumentListener on imagePathField to automatically update button text when changed/cleared
        imagePathField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String path = imagePathField.getText().trim();
                if (path.isEmpty()) {
                    browseImageButton.setText("Choose Image");
                } else {
                    File f = new File(path);
                    browseImageButton.setText("✓ " + f.getName());
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });
        
        // Action to browse images
        browseImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a Product Image");
            
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                "Supported Images (JPG, JPEG, PNG, GIF)", "jpg", "jpeg", "png", "gif"
            );
            fileChooser.setFileFilter(imageFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            
            int result = fileChooser.showOpenDialog(InventoryGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File sourceFile = fileChooser.getSelectedFile();
                File targetDir = new File("imgs");
                
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                
                File targetFile = new File(targetDir, sourceFile.getName());
                try {
                    java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    imagePathField.setText("imgs/" + sourceFile.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InventoryGUI.this, "Error copying image to local folder: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        formPanel.add(nameField);
        formPanel.add(priceField);
        formPanel.add(stockField);
        formPanel.add(browseImageButton);
        // Action Buttons Panel
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setOpaque(false);
        addButton = new CafeTheme.CafeButton("Add Product", CafeTheme.CafeButton.Variant.PRIMARY);
        clearSelectionButton = new CafeTheme.CafeButton("Clear Selection", CafeTheme.CafeButton.Variant.SECONDARY);
        checkStockButton = new CafeTheme.CafeButton("Check Low Stock", CafeTheme.CafeButton.Variant.SECONDARY);

        addButton.setPreferredSize(new Dimension(130, 36));
        clearSelectionButton.setPreferredSize(new Dimension(130, 36));
        checkStockButton.setPreferredSize(new Dimension(150, 36));

        // Low Stock Threshold Configuration
        SpinnerModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
        thresholdSpinner = new JSpinner(spinnerModel);
        thresholdSpinner.setBorder(BorderFactory.createLineBorder(CafeTheme.CREAM_DARK, 1, true));
        JComponent editor = thresholdSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
        
        JPanel thresholdContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        thresholdContainer.setOpaque(false);
        JLabel thresholdLabel = new JLabel("Low Stock Threshold:");
        thresholdLabel.setFont(CafeTheme.BOLD_FONT);
        thresholdLabel.setForeground(CafeTheme.TEXT_MUTED);
        thresholdContainer.add(thresholdLabel);
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
            imagePathField.setText("");
        });

        // Grouping the form and buttons at the bottom (SOUTH)
        mainBottomPanel = new JPanel(new BorderLayout());
        mainBottomPanel.setOpaque(false);
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
                
                // If a valid row and exactly column 4 (Delete) was clicked
                if (row >= 0 && col == 4) { 
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
    public JTextField getImagePathField() { return imagePathField; }
    public JSpinner getThresholdSpinner() { return thresholdSpinner; } 

    // --- Inner class that draws a rounded terracotta button in the table cell ---
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final Font buttonFont = new Font("SansSerif", Font.BOLD, 12);
        private final int btnW = 28;
        private final int btnH = 22;

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background rounded rectangle
            g2.setColor(CafeTheme.DANGER_TERRACOTTA);
            int btnX = (getWidth() - btnW) / 2;
            int btnY = (getHeight() - btnH) / 2;
            g2.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);
            
            // Draw the centered "✕" symbol
            g2.setColor(Color.WHITE);
            g2.setFont(buttonFont);
            FontMetrics fm = g2.getFontMetrics(buttonFont);
            String text = "✕";
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent() + fm.getDescent();
            int tx = btnX + (btnW - textWidth) / 2;
            int ty = btnY + (btnH - textHeight) / 2 + fm.getAscent() - 1; // Visually centered
            
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }
}