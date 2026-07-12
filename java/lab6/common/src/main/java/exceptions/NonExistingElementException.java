package exceptions;

public class NonExistingElementException extends Exception{
    public NonExistingElementException(String message) {
        super(message);
    }
}
