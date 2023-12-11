package exceptions;

public class IllegalWindowMove extends Exception {
    private String errorMessage = "Error=(";

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public IllegalWindowMove() {
        super();
    }

    public IllegalWindowMove(String e) {
        super();
        setErrorMessage(e);
    }

    @Override
    public String toString() {
        return this.getErrorMessage();
    }
}
