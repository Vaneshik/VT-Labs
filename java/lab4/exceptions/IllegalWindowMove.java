package exceptions;

import room.furniture.Window;

public class IllegalWindowMove extends Exception {
    Boolean window_status;

    public IllegalWindowMove(Window w){
        window_status = w.isOpen();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ", Window Status = " + window_status;
    }

    @Override
    public String toString() {
        return super.getMessage();
    }
}
