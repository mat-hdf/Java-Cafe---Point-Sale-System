import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class OrderGUI extends JFrame
{
    //component declarations
    private JPanel menuPanel, orderPanel, totalPanel;
    private JButton pie, cake, coffee, tea, water, capuccino;
    private JButton orderButton;
    private JScrollPane orderScroll;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel orderTotal, orderValue;

    public OrderGUI()
    {
        //window settings
        setTitle("Java Cafe");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //guarantees app stops running
        setLayout(new BorderLayout());

        //button declarations
        pie = new JButton("Pie");
        cake = new JButton("cake");
        coffee = new JButton("coffee");
        tea = new JButton("tea");
        water = new JButton("water");
        capuccino = new JButton("capuccino");

        //settings for the menu
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 2));
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        menuPanel.add(createButtonWrapper(pie));
        menuPanel.add(createButtonWrapper(cake));
        menuPanel.add(createButtonWrapper(coffee));
        menuPanel.add(createButtonWrapper(tea));
        menuPanel.add(createButtonWrapper(water));
        menuPanel.add(createButtonWrapper(capuccino));
        add(menuPanel, BorderLayout.CENTER);

        //settings for order listing through Jtable
        String[] columnNames = {"Quantity", "Item", "Price ($)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(tableModel);
        orderScroll = new JScrollPane(orderTable);
        orderScroll.setPreferredSize(new Dimension(250, 0));

        //setting top price
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        orderTotal = new JLabel("Total: $ ");
        orderTotal.setFont(new Font("Arial", Font.BOLD, 16));
        orderValue = new JLabel("0.00");
        orderValue.setFont(new Font("Arial", Font.BOLD, 16));
        orderValue.setForeground(new Color(0, 150, 0));
        totalPanel.add(orderTotal);
        totalPanel.add(orderValue);

        //setting order panel
        orderPanel  = new JPanel(); //panel for all order related components
        orderButton = new JButton("Place Order");
        orderPanel.setLayout(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Your Order"));
        orderPanel.add(orderScroll, BorderLayout.CENTER);
        orderPanel.add(orderButton, BorderLayout.SOUTH);
        orderPanel.add(totalPanel, BorderLayout.NORTH);
        add(orderPanel, BorderLayout.EAST);
    }

    private JPanel createButtonWrapper(JButton button) 
    {
        button.setPreferredSize(new Dimension(120, 40)); 
        JPanel wrapperPanel = new JPanel(new GridBagLayout()); 
        wrapperPanel.add(button); 
        
        return wrapperPanel;
    }
}
