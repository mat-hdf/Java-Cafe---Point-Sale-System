import javax.swing.SwingUtilities;

public class Main
{
    public static void main(String[] args)
    {
            // Run GUI construction on Event Dispatch Thread to ensure the application runs smoothly
    SwingUtilities.invokeLater(() -> 
    {      //lambda expression to create and show the GUI as soon as EDT is ready
        OrderGUI cafe = new OrderGUI();     //create GUI instance
        cafe.setVisible(true);                  //show window - JFrame is invisible by default
    });
    }
}