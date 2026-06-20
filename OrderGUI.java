import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrderGUI extends JPanel
{
    // Component declarations
    private JPanel menuPanel, orderPanel, totalPanel, finalizeOrder, storePanel, obsPanel, welcomePanel, textPanel;
    private JButton orderButton, cancelButton, removeButton;
    private JScrollPane orderScroll, obsScrollPane;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel orderTotal, orderValue, obsLabel, welcomeLabel, text, orderSubTotal, subValue, orderTax, taxValue;
    private JTextArea obs;

    public OrderGUI()
    {
        setLayout(new BorderLayout());

        // Top of page, welcome msg
        welcomePanel = new JPanel(); 
        welcomeLabel = new JLabel("Welcome to Java Cafe!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel);
        add(welcomePanel, BorderLayout.NORTH);

        // Settings for the store menu base container
        storePanel = new JPanel();
        storePanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        storePanel.setLayout(new BorderLayout());

        textPanel = new JPanel();
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        text = new JLabel("Please, place your order:");
        text.setFont(new Font("Arial", Font.BOLD, 18));
        textPanel.add(text);
        storePanel.add(textPanel, BorderLayout.NORTH);

        obsPanel = new JPanel();
        obsPanel.setLayout(new BorderLayout());
        obsLabel = new JLabel("Observations: ");
        obs = new JTextArea(5, 20);
        obs.setLineWrap(true);    
        obs.setWrapStyleWord(true);
        obsScrollPane = new JScrollPane(obs);
        obsPanel.add(obsLabel, BorderLayout.NORTH);
        obsPanel.add(obsScrollPane, BorderLayout.CENTER);

        // Layout switches to a dynamic grid (flexible row mapping count, 2 item columns)
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(0, 2, 10, 10));

        storePanel.add(menuPanel, BorderLayout.CENTER);
        storePanel.add(obsPanel, BorderLayout.SOUTH);
        add(storePanel, BorderLayout.CENTER);

        // Settings for order listing through Jtable
        String[] columnNames = {"Quantity", "Item", "Price ($)"};

        tableModel = new DefaultTableModel(columnNames, 0) 
        {
            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return false; 
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.getTableHeader().setReorderingAllowed(false);    
        orderScroll = new JScrollPane(orderTable);
        orderScroll.setPreferredSize(new Dimension(250, 0));

        // Setting prices panel structure
        totalPanel = new JPanel();
        totalPanel.setLayout(new GridLayout(3, 2, 5, 5));
        orderSubTotal = new JLabel("Subtotal: $ ");
        orderSubTotal.setFont(new Font("Arial", Font.BOLD, 16));
        subValue = new JLabel("0.00");
        subValue.setFont(new Font("Arial", Font.BOLD, 16));
        orderTax = new JLabel("Tax: $ ");
        orderTax.setFont(new Font("Arial", Font.BOLD, 16));
        taxValue = new JLabel("0.00");
        taxValue.setFont(new Font("Arial", Font.BOLD, 16));
        orderTotal = new JLabel("Total: $ ");
        orderTotal.setFont(new Font("Arial", Font.BOLD, 16));
        orderValue = new JLabel("0.00");
        orderValue.setFont(new Font("Arial", Font.BOLD, 16));
        orderValue.setForeground(new Color(0, 150, 0));
        totalPanel.add(orderSubTotal);
        totalPanel.add(subValue);
        totalPanel.add(orderTax);
        totalPanel.add(taxValue);
        totalPanel.add(orderTotal);
        totalPanel.add(orderValue);

        // Setting execution order panel
        cancelButton = new JButton("Cancel Order");
        orderButton = new JButton("Place Order");
        removeButton = new JButton("Remove Item");
        orderPanel  = new JPanel(); 
        finalizeOrder = new JPanel();
        
        finalizeOrder.setLayout(new FlowLayout(FlowLayout.RIGHT));
        finalizeOrder.add(removeButton);
        finalizeOrder.add(cancelButton);
        finalizeOrder.add(orderButton);

        orderPanel.setLayout(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Your Order"));
        orderPanel.add(orderScroll, BorderLayout.CENTER);
        orderPanel.add(finalizeOrder, BorderLayout.SOUTH);
        orderPanel.add(totalPanel, BorderLayout.NORTH);
        add(orderPanel, BorderLayout.EAST);
    }
    
    /**
     * Clears and builds menu blocks on demand pulling live components matching the inventory matrix
     */
    public void refreshMenu(DefaultTableModel inventoryModel, OrderLogic logic) {
        menuPanel.removeAll();
        
        for (int i = 0; i < inventoryModel.getRowCount(); i++) {
            String itemName = (String) inventoryModel.getValueAt(i, 0);
            String imagePath = (String) inventoryModel.getValueAt(i, 3);
            
            JButton itemButton = new JButton(itemName);
            itemButton.setActionCommand(itemName);
            itemButton.addActionListener(logic);
            
            menuPanel.add(createItemWrapper(itemButton, imagePath));
        }
        
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    public void setController(OrderLogic logic) 
    {
        removeButton.addActionListener(logic);
        cancelButton.addActionListener(logic);
        orderButton.addActionListener(logic);
    }

    private JPanel createItemWrapper(JButton button, String imagePath) 
    {
        ImageIcon icon = createResizedIcon(imagePath, 80, 80);  
        JLabel iconLabel = new JLabel(icon);    
        button.setPreferredSize(new Dimension(110, 40)); 
        
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); 
        wrapperPanel.add(iconLabel);
        wrapperPanel.add(button); 
        
        return wrapperPanel;
    }

    /**
     * Safe image builder module with a physical visible fallback text placeholder if files do not exist locally
     */
    private ImageIcon createResizedIcon(String path, int width, int height) 
    {
        try {
            File imgFile = new File(path);
            ImageIcon originalIcon;
            if (path != null && imgFile.exists() && !imgFile.isDirectory()) {
                originalIcon = new ImageIcon(path);
            } else {
                // Return a visual gray block placeholder instead of transparent nothingness if image is missing
                java.awt.image.BufferedImage placeholder = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g = placeholder.createGraphics();
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, width, height);
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("No Img", 20, height / 2 + 5);
                g.dispose();
                return new ImageIcon(placeholder);
            }
            Image img = originalIcon.getImage();
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); 
            return new ImageIcon(scaledImg);   
        } catch(Exception e) {
            return new ImageIcon(new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
    }

    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getOrderTable() { return orderTable; }
    public JLabel getOrderValueLabel() { return orderValue; }
    public JLabel getSubLabel() { return subValue; }
    public JLabel getTaxLabel() { return taxValue; }
    public JTextArea getObsTextArea() { return obs; }
}