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

public class OrderLogic implements ActionListener 
{
    private OrderGUI gui;   
    private double currentTotal = 0.0, currentSub = 0.0, currentTax = 0.0;  
    private SalesReportGUI reportScreen;
    private InventoryLogic inventoryLogic;
    private double tax = 0.1;

    public OrderLogic(OrderGUI gui) 
    {
        this.gui = gui;
    }

    public void setReportScreen(SalesReportGUI reportScreen) 
    {
        this.reportScreen = reportScreen;
    }

    public void setInventoryLogic(InventoryLogic inventoryLogic) 
    {
        this.inventoryLogic = inventoryLogic;
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        String button = e.getActionCommand(); 

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
                if (inventoryLogic != null) {
                    double price = inventoryLogic.getPrice(button);
                    if (price > 0) {
                        addItem(button, price);
                    }
                }
                break;
        }
    }

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
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));  
    }

    private void removeItem() 
    {
        int selection = gui.getOrderTable().getSelectedRow();
        if (selection != -1)
        { 
            Object qtyObj = gui.getTableModel().getValueAt(selection, 0);   
            int currentQty = (qtyObj instanceof Number) ? ((Number) qtyObj).intValue() : Integer.parseInt(qtyObj.toString());

            String priceStr = (String) gui.getTableModel().getValueAt(selection, 2);    
            double itemPrice = Double.parseDouble(priceStr.replace(",", "."));  
            double unitPrice = itemPrice / currentQty;  

            currentSub -= unitPrice;    
            currentTax -= (unitPrice * tax);    

            if (currentSub < 0.01)  
            {
                currentSub = 0.0;
                currentTax = 0.0;
            }

            currentTotal = currentSub + currentTax; 
            
            if (currentTotal < 0)   
            {
                gui.getSubLabel().setText(String.format("%.2f", 0.0));
                gui.getTaxLabel().setText(String.format("%.2f", 0.0));
                gui.getOrderValueLabel().setText(String.format("%.2f", 0.0));
            }
            else    
            {
                gui.getSubLabel().setText(String.format("%.2f", currentSub));
                gui.getTaxLabel().setText(String.format("%.2f", currentTax));
                gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal));
            }
            
            if (currentQty > 1)     
            {
                int newQty = currentQty - 1;    
                gui.getTableModel().setValueAt(newQty, selection, 0);   
                double newRowTotal = newQty * unitPrice;    
                gui.getTableModel().setValueAt(String.format("%.2f", newRowTotal), selection, 2);  
            }
            else
            {
                gui.getTableModel().removeRow(selection);   
            }
        }
        else    
        {
            JOptionPane.showMessageDialog(gui, "Please click on an item to remove it", "No item selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cancelOrder()
    {
        gui.getTableModel().setRowCount(0);  

        currentSub = 0.0;
        currentTax = 0.0;
        currentTotal = 0.0;
        gui.getSubLabel().setText(String.format("%.2f", currentSub));
        gui.getTaxLabel().setText(String.format("%.2f", currentTax));
        gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); 
        gui.getObsTextArea().setText("");   
    }

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
                    return; 
                }
            }

            SalesPersistence.saveSale(items);

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

            gui.getTableModel().setRowCount(0);  
    
            currentSub = 0.0;
            currentTax = 0.0;
            currentTotal = 0.0;
            gui.getSubLabel().setText(String.format("%.2f", currentSub));
            gui.getTaxLabel().setText(String.format("%.2f", currentTax));
            gui.getOrderValueLabel().setText(String.format("%.2f", currentTotal)); 
            
            JOptionPane.showMessageDialog(gui, "Order submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            gui.getObsTextArea().setText("");   
        }
        else
        {
            JOptionPane.showMessageDialog(gui, "Error! Order empty!", "Invalid Order", JOptionPane.ERROR_MESSAGE);
            gui.getObsTextArea().setText("");   
        }
    }

    private void printReceipt() 
    {
        StringBuilder receipt = new StringBuilder();

        receipt.append("========================================\n");
        receipt.append("             CAFE RECEIPT               \n");
        receipt.append("========================================\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        receipt.append("Date: ").append(dtf.format(now)).append("\n\n");

        receipt.append(String.format("%-5s %-20s %-10s\n", "Qtd", "Item", "Total"));
        receipt.append("----------------------------------------\n");

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

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea); 
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 500));

        Object[] options = {"Print", "Save to File", "Close"};  
        int choice = JOptionPane.showOptionDialog(
            gui,    
            scrollPane,     
            "Cafe Receipt",     
            JOptionPane.YES_NO_CANCEL_OPTION,   
            JOptionPane.PLAIN_MESSAGE,  
            null, 
            options,    
            options[2]  
        );

        if (choice == 0)    
        {
            try 
            {
                textArea.print(); 
            } 
            catch (PrinterException ex) 
            {
                JOptionPane.showMessageDialog(gui, "Error printing receipt: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (choice == 1)
        {
            try 
            {
                String fileName = "receipt_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName)))
                {
                    writer.print(receipt.toString());
                }
                JOptionPane.showMessageDialog(gui, "Receipt saved successfully to: " + fileName, "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } 
            catch (IOException ex) 
            {
                JOptionPane.showMessageDialog(gui, "Error saving receipt: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}