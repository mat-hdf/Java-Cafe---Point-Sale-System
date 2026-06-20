import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Controller class handling the business logic for the order screen.
 * Processes dynamically generated item additions, removals, cancellations, and checkout submissions.
 */
public class OrderLogic implements ActionListener 
{
    private OrderGUI gui;   // receives GUI to access its public methods and components to update visual components
    private double currentTotal = 0.0, currentSub = 0.0, currentTax = 0.0;  // updates total order value
    private SalesReportGUI reportScreen;
    private InventoryLogic inventoryLogic;
    private double tax = 0.1;

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
     * Sets the SalesReportGUI screen to update reports on order submission.
     *
     * @param reportScreen the SalesReportGUI screen instance
     */
    public void setReportScreen(SalesReportGUI reportScreen) 
    {
        this.reportScreen = reportScreen;
    }

    /**
     * Sets the InventoryLogic controller to handle stock operations.
     *
     * @param inventoryLogic the InventoryLogic controller instance
     */
    public void setInventoryLogic(InventoryLogic inventoryLogic) 
    {
        this.inventoryLogic = inventoryLogic;
    }

    /**
     * Listens for action events triggered from GUI item buttons or sidebar controls.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        // actionCommand returns a string with the components action command, in this case, the buttons text on it
        String button = e.getActionCommand(); 

        // from that, we can get which specific button was pressed and trigger the respective functionality
        switch (button) 
        {
            case "Remove Item":
                removeItem();
                break;
            case "Cancel Order":
                cancelOrder();
                break;
            case "Place Order":
                submitOrder();
                break;
            default:
                // Dynamic item action matching execution queries mapped against the inventory lookup catalog
                // By doing this, we can get which menu item button was pressed and add that item to the order dynamically
                if (inventoryLogic != null) {
                    double price = inventoryLogic.getPrice(button);
                    if (price > 0) {
                        addItem(button, price);
                    }
                }
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
        DefaultTableModel model = gui.getTableModel();
        boolean itemExists = false;

        for(int i = 0; i < model.getRowCount(); i++)
        {
            String existingItemName = (String) model.getValueAt(i, 1);
            if (existingItemName.equals(item))
            {
                Object qtyObj = model.getValueAt(i, 0);
                int currentQty = (qtyObj instanceof Number) ? ((Number) qtyObj).intValue() : Integer.parseInt(qtyObj.toString());
                int newQty = currentQty + 1;

                model.setValueAt(newQty, i, 0);

                double newRowTotal = newQty * price;
                model.setValueAt(String.format("%.2f", newRowTotal), i, 2);

                itemExists = true;
                break;
            }
        }

        if (!itemExists) 
        {
            model.addRow(new Object[]{1, item, String.format("%.2f", price)});
        }

        currentSub += price;
        currentTax += price * tax;
        currentTotal = currentSub + currentTax;

        gui.getSubLabel().setText(String.format("%.2f", currentSub));
        gui.getTaxLabel().setText(String.format("%.2f", currentTax));
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));  // sets new total to JLabel holding order value
    }

    /**
     * Removes the currently selected item from the order table and updates the total.
     */
    private void removeItem() 
    {
        int selection = gui.getOrderTable().getSelectedRow();
        if (selection != -1)
        { 
            Object qtyObj = gui.getTableModel().getValueAt(selection, 0);   // gets quanttity from table
            int currentQty = (qtyObj instanceof Number) ? ((Number) qtyObj).intValue() : Integer.parseInt(qtyObj.toString());
            // verifies if numeric, if so, casts to Number and extracts values, otherwise parses it

            String priceStr = (String) gui.getTableModel().getValueAt(selection, 2);    // gets price at column
            double itemPrice = Double.parseDouble(priceStr.replace(",", "."));  // replaces , by . to avoid errors
            double unitPrice = itemPrice / currentQty;  // gets single unit price

            currentSub -= unitPrice;    // removes price of 1 unit
            currentTax -= (unitPrice * tax);    // removes tax

            if (currentSub < 0.01)  // avoid aproximation errors
            {
                currentSub = 0.0;
                currentTax = 0.0;
            }

            currentTotal = currentSub + currentTax; 
            
            if (currentTotal < 0)   // sets everything to 0
            {
                gui.getSubLabel().setText(String.format("%.2f", 0.0));
                gui.getTaxLabel().setText(String.format("%.2f", 0.0));
                gui.getOrderValueLabel().setText(String.format("%.2f", 0.0));
            }
            else    // updates UI
            {
                gui.getSubLabel().setText(String.format("%.2f", currentSub));
                gui.getTaxLabel().setText(String.format("%.2f", currentTax));
                gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));
            }
            
            if (currentQty > 1)     // if more than one, does not remove row
            {
                int newQty = currentQty - 1;    // updates qty
                gui.getTableModel().setValueAt(newQty, selection, 0);   // sets value to table
                double newRowTotal = newQty * unitPrice;    // updates total of said item
                gui.getTableModel().setValueAt(String.format("%.2f", newRowTotal), selection, 2);  // updates UI
            }
            else
            {
                gui.getTableModel().removeRow(selection);   // if only 1, removes row
            }
        }
        else    // error msg
        {
            JOptionPane.showMessageDialog(gui, "Please click on an item to remove it", "No item selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Cancels the current order, clearing all items and resetting the total to 0.
     */
    private void cancelOrder()
    {
        gui.getTableModel().setRowCount(0);  // nullifies all table rows

        currentSub = 0.0;
        currentTax = 0.0;
        currentTotal = 0.0;
        gui.getSubLabel().setText(String.format("%.2f", currentSub));
        gui.getTaxLabel().setText(String.format("%.2f", currentTax));
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); // updates total
        gui.getObsTextArea().setText("");   // clears observations text
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
                double unitPrice = price / qty;
                items.add(new SalesPersistence.SaleItem(name, qty, unitPrice));
            }

            // Check stock availability before proceeding
            if (inventoryLogic != null)
            {
                try
                {
                    for (SalesPersistence.SaleItem item : items)
                    {
                        int stock = inventoryLogic.getStock(item.getName());
                        if (stock < item.getQuantity())
                        {
                            throw new OutOfStockException("Item '" + item.getName() + "' is out of stock or has insufficient quantity! (Available: " + stock + ")");
                        }
                    }
                }
                catch (OutOfStockException ex)
                {
                    JOptionPane.showMessageDialog(gui, ex.getMessage(), "Out of Stock Error", JOptionPane.ERROR_MESSAGE);
                    return; // Aborts submitting the order
                }
            }

            SalesPersistence.saveSale(items);

            // Decrease inventory stock
            if (inventoryLogic != null)
            {
                for (SalesPersistence.SaleItem item : items)
                {
                    inventoryLogic.decreaseStock(item.getName(), item.getQuantity());
                }
            }

            if (reportScreen != null) 
            {
                reportScreen.refresh();
            }

            printReceipt();

            gui.getTableModel().setRowCount(0);  // nullifies all table rows
    
            currentSub = 0.0;
            currentTax = 0.0;
            currentTotal = 0.0;
            gui.getSubLabel().setText(String.format("%.2f", currentSub));
            gui.getTaxLabel().setText(String.format("%.2f", currentTax));
            gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); // updates total
            
            // creates a pop-up to let user know the order was a success
            JOptionPane.showMessageDialog(gui, "Order submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            gui.getObsTextArea().setText("");   // clears observations text
        }
        else
        {
            // pop-up with error message in case of empty order
            JOptionPane.showMessageDialog(gui, "Error! Order empty!", "Invalid Order", JOptionPane.ERROR_MESSAGE);
            gui.getObsTextArea().setText("");   // clears observations text
        }
    }

    /**
     * Generates a text-based receipt and displays it in a dialog, allowing the user to print or save it.
     */
    private void printReceipt() 
    {
        StringBuilder receipt = new StringBuilder();

        receipt.append("========================================\n");
        receipt.append("             CAFE RECEIPT               \n");
        receipt.append("========================================\n");

        // uses current time to place into receipt
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        receipt.append("Date: ").append(dtf.format(now)).append("\n\n");

        // headers
        receipt.append(String.format("%-5s %-20s %-10s\n", "Qtd", "Item", "Total"));
        receipt.append("----------------------------------------\n");

        // reads iteratively over table items and adds them to the receipt
        DefaultTableModel model = gui.getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) 
        {
            String qty = model.getValueAt(i, 0).toString();
            String name = model.getValueAt(i, 1).toString();
            String price = model.getValueAt(i, 2).toString();
            
            receipt.append(String.format("%-5s %-20s $ %-9s\n", qty, name, price));
        }

        receipt.append("----------------------------------------\n");
        receipt.append(String.format("Subtotal:                 $ %.2f\n", currentSub));
        receipt.append(String.format("Tax:                      $ %.2f\n", currentTax));
        receipt.append(String.format("TOTAL:                    $ %.2f\n", currentTotal));
        receipt.append("========================================\n");
        receipt.append("        Thank you for your visit!       \n");

        // displays the receipt in a dialog with print/save/close options
        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea); // adds it to a scroll pane for longer orders
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 500));

        Object[] options = {"Print", "Save to File", "Close"};  // array of options
        int choice = JOptionPane.showOptionDialog(
            gui,    // main window as parent
            scrollPane,     // pane containing the receipt as msg
            "Cafe Receipt",     // title
            JOptionPane.YES_NO_CANCEL_OPTION,   // generic button types 
            JOptionPane.PLAIN_MESSAGE,  // only displays the receipt, no additional msgs 
            null, // icon
            options,    // custom buttons
            options[2]  // initial focus
        );

        if (choice == 0)    // printing in options array
        {
            // printing the receipt
            try 
            {
                textArea.print(); 
            } 
            catch (PrinterException ex) 
            {
                // error msg
                JOptionPane.showMessageDialog(gui, "Error printing receipt: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (choice == 1)
        {
            // saving the receipt to a file
            try 
            {
                // creates a filename with timestamp
                String fileName = "receipt_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                
                // writes receipt content to the file
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName)))
                {
                    writer.print(receipt.toString());
                }
                
                // success msg
                JOptionPane.showMessageDialog(gui, "Receipt saved successfully to: " + fileName, "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } 
            catch (IOException ex) 
            {
                // error msg
                JOptionPane.showMessageDialog(gui, "Error saving receipt: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}