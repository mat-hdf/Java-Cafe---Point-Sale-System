import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Controller class handling the business logic for the order screen.
 * Processes item additions, removals, cancellations, and checkout submissions.
 */
public class OrderLogic implements ActionListener 
{
    
    private OrderGUI gui;   //receives GUI to access its public methods and components to update visual components
    private double currentTotal = 0.0;  //updates total order value
    private SalesReportLogic reportLogic;

    /**
     * Constructs a new OrderLogic controller linked to an OrderGUI window.
     *
     * @param gui the OrderGUI instance to control
     */
    public OrderLogic(OrderGUI gui) 
    {
        this.gui = gui;
    }

    /**
     * Sets the SalesReportLogic controller to update reports on order submission.
     *
     * @param reportLogic the SalesReportLogic controller instance
     */
    public void setReportLogic(SalesReportLogic reportLogic) 
    {
        this.reportLogic = reportLogic;
    }

    /**
     * Listens for action events triggered from GUI item buttons or sidebar controls.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        // actionCommand returns a string with the coponents action command, in this case, the buttons text on it
        String button = e.getActionCommand(); 

        //from that, we can get which button was pressed and add that item to the order
        switch (button) 
        {
            case "Pie":
                addItem("Pie", 4.50);
                break;
            case "Cake":
                addItem("Cake", 5.00);
                break;
            case "Coffee":
                addItem("Coffee", 2.50);
                break;
            case "Tea":
                addItem("Tea", 2.50);
                break;
            case "Water":
                addItem("Water", 1.00);
                break;
            case "Capuccino":
                addItem("Capuccino", 2.50);
                break;
            case "Remove Item":
                removeItem();
                break;
            case "Cancel Order":
                cancelOrder();
                break;
            case "Place Order":
                submitOrder();
                break;
        }
    }

    /**
     * Adds an item with a given price to the current order and updates the total.
     *
     * @param item  the item name
     * @param price the item unit price
     */
    private void addItem(String item, double price)
    {
        //accesses table with order items and adds a new line with a list holding every information (qtt, name, price)
        gui.getTableModel().addRow(new Object[]{1, item, String.format("%.2f", price)});   
        
        //adds price to current order total
        currentTotal += price;
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));  //sets new total to JLabel holding order value
    }

    /**
     * Removes the currently selected item from the order table and updates the total.
     */
    private void removeItem() 
    {
        int selection = gui.getOrderTable().getSelectedRow();   //gets the selected row on the order table
        //selected through user click on mouse
        //its returns are enumerated starting at 0
        if (selection != -1) //if nothing is selected, returns -1
        { 
            //gets a string of the price by accessing the table
            String priceStr = (String) gui.getTableModel().getValueAt(selection, 2);    
            //getValueAt searches for the value at given row (selection) and column, in this case, price is the third

            double itemPrice = Double.parseDouble(priceStr.replace(",", "."));  
            //parses doubles and switches commas to prevent crashes on OS interaction

            currentTotal -= itemPrice;  //subtracts removed item price from total
            
            if (currentTotal < 0) 
            {
                gui.getOrderValueLabel().setText(String.format("%.2f", 0.0)); //sets total to 0
            }
            else
            {
                gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); //updates total
            }
            
            gui.getTableModel().removeRow(selection);   //removes that row from the table
        }
        else
        {
            JOptionPane.showMessageDialog(gui, "Please click on an item to remove it", "No item selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Cancels the current order, clearing all items and resetting the total to 0.
     */
    private void cancelOrder()
    {
        gui.getTableModel().setRowCount(0);  //nullifies all table rows

        currentTotal = 0.0;

        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); //updates total
        gui.getObsTextArea().setText("");   //clears observations text
    }

    /**
     * Submits the current order, saving it to persistence and updating the sales reports.
     */
    private void submitOrder()
    {
        if (gui.getTableModel().getRowCount() != 0) 
        {
            List<SalesPersistence.SaleItem> items = new ArrayList<>();
            DefaultTableModel model = gui.getTableModel();
            for (int i = 0; i < model.getRowCount(); i++) 
            {
                Object qtyObj = model.getValueAt(i, 0);
                int qty = (qtyObj instanceof Number) ? ((Number) qtyObj).intValue() : Integer.parseInt(qtyObj.toString());
                String name = (String) model.getValueAt(i, 1);
                String priceStr = model.getValueAt(i, 2).toString();
                double price = Double.parseDouble(priceStr.replace(",", "."));
                items.add(new SalesPersistence.SaleItem(name, qty, price));
            }
            SalesPersistence.saveSale(items);

            if (reportLogic != null) 
            {
                reportLogic.refresh();
            }

            gui.getTableModel().setRowCount(0);  //nullifies all table rows
    
            currentTotal = 0.0;
    
            gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); //resets total after order submission
            
            JOptionPane.showMessageDialog(gui, "Order submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            //creates a pop-up to let user know the order was a success
            gui.getObsTextArea().setText("");   //clears observations text
        }
        else
        {
            JOptionPane.showMessageDialog(gui, "Error! Order empty!", "Invalid Order", JOptionPane.ERROR_MESSAGE);
            //pop-up with error message in case of empty order
            gui.getObsTextArea().setText("");   //clears observations text
        }
    }
}