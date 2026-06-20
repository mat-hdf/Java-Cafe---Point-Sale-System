import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Graphical User Interface for placing orders at Java Cafe.
 * Contains the menu layout with dynamically generated buttons for items and the sidebar listing order details.
 */
public class OrderGUI extends JPanel
{
    // component declarations
    private JPanel menuPanel, orderPanel, totalPanel, finalizeOrder, storePanel, obsPanel, welcomePanel, textPanel;
    private JButton orderButton, cancelButton, removeButton;
    private JScrollPane orderScroll, obsScrollPane;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel orderTotal, orderValue, obsLabel, welcomeLabel, text, orderSubTotal, subValue, orderTax, taxValue;
    private JTextArea obs;

    /**
     * Constructs a new OrderGUI layout, setting up panels, dynamic menus, and buttons.
     */
    public OrderGUI()
    {
        // window settings
        setLayout(new BorderLayout(15, 15));
        setBackground(CafeTheme.OFF_WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // top of page, welcome msg
        welcomePanel = new JPanel(); 
        welcomePanel.setBackground(CafeTheme.OFF_WHITE);
        welcomeLabel = new JLabel("☕ Java Café");
        welcomeLabel.setFont(CafeTheme.TITLE_FONT);
        welcomeLabel.setForeground(CafeTheme.DARK_ROAST);
        welcomePanel.add(welcomeLabel);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(welcomePanel, BorderLayout.NORTH);

        // settings for the store menu base container
        storePanel = new JPanel();
        storePanel.setBackground(CafeTheme.OFF_WHITE);
        storePanel.setBorder(CafeTheme.createCafeTitledBorder("Menu Card"));
        storePanel.setLayout(new BorderLayout(10, 10));

        textPanel = new JPanel();
        textPanel.setBackground(CafeTheme.OFF_WHITE);
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        text = new JLabel("Select items to add to order:");
        text.setFont(CafeTheme.BOLD_FONT);
        text.setForeground(CafeTheme.TEXT_MUTED);
        textPanel.add(text);
        storePanel.add(textPanel, BorderLayout.NORTH);

        obsPanel = new JPanel();
        obsPanel.setBackground(CafeTheme.OFF_WHITE);
        obsPanel.setLayout(new BorderLayout(5, 5));
        obsLabel = new JLabel("Special Instructions / Observations:");
        obsLabel.setFont(CafeTheme.BOLD_FONT);
        obsLabel.setForeground(CafeTheme.DARK_ROAST);
        
        obs = new JTextArea(4, 20);
        obs.setLineWrap(true);    
        obs.setWrapStyleWord(true);
        obs.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        
        obsScrollPane = new JScrollPane(obs);
        CafeTheme.styleScrollPane(obsScrollPane);
        obsPanel.add(obsLabel, BorderLayout.NORTH);
        obsPanel.add(obsScrollPane, BorderLayout.CENTER);
        obsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Layout switches to a dynamic grid (flexible row mapping count, 2 item columns)
        menuPanel = new JPanel();
        menuPanel.setBackground(CafeTheme.OFF_WHITE);
        menuPanel.setLayout(new GridLayout(0, 2, 12, 12));

        // Wrap menu panel in scroll pane to prevent any clipping/cutoff
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        CafeTheme.styleScrollPane(menuScroll);
        menuScroll.setBorder(BorderFactory.createEmptyBorder()); // clean seamless look

        storePanel.add(menuScroll, BorderLayout.CENTER);
        storePanel.add(obsPanel, BorderLayout.SOUTH);
        add(storePanel, BorderLayout.CENTER);

        // settings for order listing through Jtable
        String[] columnNames = {"Quantity", "Item", "Price ($)"};

        tableModel = new DefaultTableModel(columnNames, 0) 
        {
            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return false; // blocks cell editing
            }
        };
        orderTable = new JTable(tableModel);
        CafeTheme.styleTable(orderTable);
        
        orderScroll = new JScrollPane(orderTable);
        CafeTheme.styleScrollPane(orderScroll);
        orderScroll.setPreferredSize(new Dimension(280, 0));

        // setting top price panel structure
        totalPanel = new JPanel();
        totalPanel.setBackground(CafeTheme.OFF_WHITE);
        totalPanel.setLayout(new GridLayout(3, 2, 5, 8));
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CafeTheme.CREAM_DARK),
            BorderFactory.createEmptyBorder(5, 5, 12, 5)
        ));

        orderSubTotal = new JLabel("Subtotal: $");
        orderSubTotal.setFont(CafeTheme.REGULAR_FONT);
        orderSubTotal.setForeground(CafeTheme.TEXT_MUTED);
        
        subValue = new JLabel("0.00");
        subValue.setFont(CafeTheme.BOLD_FONT);
        subValue.setForeground(CafeTheme.DARK_ROAST);
        subValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        orderTax = new JLabel("Tax (10%): $");
        orderTax.setFont(CafeTheme.REGULAR_FONT);
        orderTax.setForeground(CafeTheme.TEXT_MUTED);
        
        taxValue = new JLabel("0.00");
        taxValue.setFont(CafeTheme.BOLD_FONT);
        taxValue.setForeground(CafeTheme.DARK_ROAST);
        taxValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        orderTotal = new JLabel("Total: $");
        orderTotal.setFont(CafeTheme.SUBTITLE_FONT);
        orderTotal.setForeground(CafeTheme.DARK_ROAST);
        
        orderValue = new JLabel("0.00");
        orderValue.setFont(new Font("SansSerif", Font.BOLD, 18));
        orderValue.setForeground(CafeTheme.CARAMEL);
        orderValue.setHorizontalAlignment(SwingConstants.RIGHT);

        totalPanel.add(orderSubTotal);
        totalPanel.add(subValue);
        totalPanel.add(orderTax);
        totalPanel.add(taxValue);
        totalPanel.add(orderTotal);
        totalPanel.add(orderValue);

        // setting order execution panel
        cancelButton = new CafeTheme.CafeButton("Cancel Order", CafeTheme.CafeButton.Variant.DANGER);
        orderButton = new CafeTheme.CafeButton("Place Order", CafeTheme.CafeButton.Variant.SUCCESS);
        removeButton = new CafeTheme.CafeButton("Remove Item", CafeTheme.CafeButton.Variant.SECONDARY);
        
        cancelButton.setPreferredSize(new Dimension(110, 36));
        orderButton.setPreferredSize(new Dimension(110, 36));
        removeButton.setPreferredSize(new Dimension(110, 36));

        orderPanel  = new JPanel(); // panel for all order related components
        orderPanel.setBackground(CafeTheme.OFF_WHITE);
        finalizeOrder = new JPanel();
        finalizeOrder.setBackground(CafeTheme.OFF_WHITE);
        
        finalizeOrder.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        finalizeOrder.add(removeButton);
        finalizeOrder.add(cancelButton);
        finalizeOrder.add(orderButton);

        orderPanel.setLayout(new BorderLayout(10, 10));
        orderPanel.setBorder(CafeTheme.createCafeTitledBorder("Your Order"));
        orderPanel.add(orderScroll, BorderLayout.CENTER);
        orderPanel.add(finalizeOrder, BorderLayout.SOUTH);
        orderPanel.add(totalPanel, BorderLayout.NORTH);
        add(orderPanel, BorderLayout.EAST);
    }
    
    /**
     * Clears and rebuilds the menu layout dynamically based on the current inventory.
     * * @param inventoryModel the table model pulling live components matching the inventory matrix
     * @param logic          the OrderLogic controller handling actions
     */
    public void refreshMenu(DefaultTableModel inventoryModel, OrderLogic logic) {
        menuPanel.removeAll();
        
        for (int i = 0; i < inventoryModel.getRowCount(); i++) {
            String itemName = (String) inventoryModel.getValueAt(i, 0);
            String imagePath = (String) inventoryModel.getValueAt(i, 3);
            
            JButton itemButton = new CafeTheme.CafeButton(itemName, CafeTheme.CafeButton.Variant.PRIMARY);
            itemButton.setActionCommand(itemName);
            itemButton.addActionListener(logic);
            
            menuPanel.add(createItemWrapper(itemButton, imagePath));
        }
        
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    /**
     * Sets the controller logic for handling order-related button clicks.
     *
     * @param logic the OrderLogic controller instance
     */
    public void setController(OrderLogic logic) 
    {
        // adding listeners to sidebar buttons
        removeButton.addActionListener(logic);
        cancelButton.addActionListener(logic);
        orderButton.addActionListener(logic);
    }

    /**
     * Wraps a button with its corresponding resized item icon.
     *
     * @param button    the action button to wrap
     * @param imagePath the filesystem path to the icon image
     * @return the wrapped JPanel containing icon and button
     */
    private JPanel createItemWrapper(JButton button, String imagePath) 
    {
        ImageIcon icon = createResizedIcon(imagePath, 75, 75);  // resizes img to 75x75
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        button.setPreferredSize(new Dimension(125, 38)); // button size
        
        // A sleek, rounded menu card panel with GridBagLayout for side-by-side alignment
        JPanel itemCard = new JPanel(new GridBagLayout());
        itemCard.setBackground(Color.WHITE);
        itemCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CafeTheme.CREAM_DARK, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.CENTER;
        itemCard.add(iconLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        itemCard.add(button, gbc);
        
        return itemCard;
    }

    /**
     * Resizes an image file to the target width and height to use as an icon.
     * Implements a physical visible fallback text placeholder if files do not exist locally.
     *
     * @param path   the image file path
     * @param width  the target width
     * @param height the target height
     * @return a resized ImageIcon or a fallback placeholder block
     */
    private ImageIcon createResizedIcon(String path, int width, int height) 
    {
        try {
            File imgFile = new File(path);
            ImageIcon originalIcon;
            if (path != null && imgFile.exists() && !imgFile.isDirectory()) {
                originalIcon = new ImageIcon(path);
            } else {
                // Return a beautiful vector coffee cup placeholder instead of a gray block
                java.awt.image.BufferedImage placeholder = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = placeholder.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Soft cream background
                g2.setColor(CafeTheme.CREAM_LIGHT);
                g2.fillRoundRect(0, 0, width, height, 16, 16);
                g2.setColor(CafeTheme.CREAM_DARK);
                g2.drawRoundRect(0, 0, width - 1, height - 1, 16, 16);
                
                // Soft orange/caramel coffee cup icon
                g2.setColor(CafeTheme.CARAMEL);
                // Cup body
                int cupW = 34;
                int cupH = 26;
                int cupX = (width - cupW) / 2 - 3;
                int cupY = (height - cupH) / 2 + 3;
                g2.fillRoundRect(cupX, cupY, cupW, cupH, 8, 8);
                
                // Cup handle
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cupX + cupW - 6, cupY + 4, 12, 14, 270, 180);
                
                // Saucer/Plate
                g2.drawLine(cupX - 6, cupY + cupH, cupX + cupW + 6, cupY + cupH);
                
                // Steam waves
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cupX + 8, cupY - 10, 6, 8, 90, 180);
                g2.drawArc(cupX + 16, cupY - 10, 6, 8, 270, 180);
                
                g2.dispose();
                return new ImageIcon(placeholder);
            }
            Image img = originalIcon.getImage();
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // resizes it
            return new ImageIcon(scaledImg);    // returns adjusted img
        } catch(Exception e) {
            return new ImageIcon(new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
    }

    /**
     * Gets the table model listing order items.
     *
     * @return the JTable's DefaultTableModel
     */
    public DefaultTableModel getTableModel() { return tableModel; }
    
    /**
     * Gets the JTable representing the order items.
     *
     * @return the order JTable
     */
    public JTable getOrderTable() { return orderTable; }
    
    /**
     * Gets the label displaying the current order total value.
     *
     * @return the Jlabel representing the total order value
     */
    public JLabel getOrderValueLabel() { return orderValue; }
    
    /**
     * Gets the label displaying the current order subtotal value.
     *
     * @return the Jlabel representing the subtotal value
     */
    public JLabel getSubLabel() { return subValue; }
    
    /**
     * Gets the label displaying the current order tax value.
     *
     * @return the Jlabel representing the tax value
     */
    public JLabel getTaxLabel() { return taxValue; }
    
    /**
     * Gets the observation text area.
     *
     * @return the observation JTextArea
     */
    public JTextArea getObsTextArea() { return obs; }
}