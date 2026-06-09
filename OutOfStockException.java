/**
 * Custom Exception thrown when an item is out of stock in the inventory.
 */
public class OutOfStockException extends Exception {
    /**
     * Constructs a new OutOfStockException with a detailed message.
     *
     * @param message the detail message
     */
    public OutOfStockException(String message) {
        super(message);
    }
}
