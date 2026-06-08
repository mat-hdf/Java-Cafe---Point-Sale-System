import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrderGUI extends JPanel
{
    //component declarations
    private JPanel menuPanel, orderPanel, totalPanel, finalizeOrder, storePanel, obsPanel, welcomePanel, textPanel;
    private JButton pie, cake, coffee, tea, water, capuccino;
    private JButton orderButton, cancelButton, removeButton;
    private JScrollPane orderScroll, obsScrollPane;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel orderTotal, orderValue, obsLabel, welcomeLabel, text;
    private JTextArea obs;

    public OrderGUI()
    {
        // window settings
        setLayout(new BorderLayout());

        //top of page, welcome msg
        welcomePanel = new JPanel(); 
        welcomeLabel = new JLabel("Welcome to Java Cafe!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel);
        add(welcomePanel, BorderLayout.NORTH);

        //button declarations
        pie = new JButton("Pie");
        cake = new JButton("Cake");
        coffee = new JButton("Coffee");
        tea = new JButton("Tea");
        water = new JButton("Water");
        capuccino = new JButton("Capuccino");

        //settings for the menu
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

        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 2));
        menuPanel.add(createItemWrapper(pie, "imgs/pie.jpg"));
        menuPanel.add(createItemWrapper(cake, "imgs/cake.jpeg"));
        menuPanel.add(createItemWrapper(coffee, "imgs/coffee.JPG"));
        menuPanel.add(createItemWrapper(tea, "imgs/tea.jpg"));
        menuPanel.add(createItemWrapper(water, "imgs/commercial-pet-bottle.jpg"));
        menuPanel.add(createItemWrapper(capuccino, "imgs/capuccino.jpg"));

        storePanel.add(menuPanel, BorderLayout.CENTER);
        storePanel.add(obsPanel, BorderLayout.SOUTH);
        add(storePanel, BorderLayout.CENTER);

        //settings for order listing through Jtable
        String[] columnNames = {"Quantity", "Item", "Price ($)"};

        tableModel = new DefaultTableModel(columnNames, 0) 
        {
            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return false; //blocks cell editing
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.getTableHeader().setReorderingAllowed(false);    //blocks reordering table columns
        orderScroll = new JScrollPane(orderTable);
        orderScroll.setPreferredSize(new Dimension(250, 0));

        //setting top price
        totalPanel = new JPanel();
        totalPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        orderTotal = new JLabel("Total: $ ");
        orderTotal.setFont(new Font("Arial", Font.BOLD, 16));
        orderValue = new JLabel("0.00");
        orderValue.setFont(new Font("Arial", Font.BOLD, 16));
        orderValue.setForeground(new Color(0, 150, 0));
        totalPanel.add(orderTotal);
        totalPanel.add(orderValue);

        //setting order panel
        cancelButton = new JButton("Cancel Order");
        orderButton = new JButton("Place Order");
        removeButton = new JButton("Remove Item");
        orderPanel  = new JPanel(); //panel for all order related components
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
    
    public void setController (OrderLogic logic) 
    {
        //adding listeners to all buttons
        pie.addActionListener(logic);
        cake.addActionListener(logic);
        coffee.addActionListener(logic);
        tea.addActionListener(logic);
        water.addActionListener(logic);
        capuccino.addActionListener(logic);
        removeButton.addActionListener(logic);
        cancelButton.addActionListener(logic);
        orderButton.addActionListener(logic);
    }

    private JPanel createItemWrapper(JButton button, String imagePath) 
    {

        ImageIcon icon = createResizedIcon(imagePath, 80, 80);  //resizes img

        JLabel iconLabel = new JLabel(icon);    //creates new JLabel with resized img
        
        button.setPreferredSize(new Dimension(100, 40)); //button size
        
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); //creates a jpanel with img and button
        
        wrapperPanel.add(iconLabel);
        wrapperPanel.add(button); 
        
        return wrapperPanel;
    }

    private ImageIcon createResizedIcon(String path, int width, int height) //auxiliar method, receives img path and preferred size
    {
        ImageIcon originalIcon = new ImageIcon(path);

        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); //resizes it
        return new ImageIcon(scaledImg);    //returns adjusted img
    }

    //methods to grant access to elements that must be updated within OrderLogic
    public DefaultTableModel getTableModel()
    {
        return tableModel;
    }

    public JTable getOrderTable()
    {
        return orderTable;
    }

    public JLabel getOrderValueLabel()
    {
        return orderValue;
    }

    public JTextArea getObsTextArea() 
    { 
        return obs; 
    }
}
