package exceptions;

import room.Window;

public class IllegalWindowMove extends Exception {
    private final Boolean window_status;
    private final String message;

    public IllegalWindowMove(String m, Window w){
        window_status = w.isOpen();
        message = m;
    }

    @Override
    public String getMessage() {
        return message + " Window Status = " + window_status;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
