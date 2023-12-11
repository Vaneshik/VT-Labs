package room.furniture;

import exceptions.IllegalWindowMove;

public class Window {
    private boolean isOpen = false;

    public void open() throws IllegalWindowMove {
        if (!this.isOpen) throw new IllegalWindowMove(this + " уже открыто!");
        this.isOpen = true;
    }

    public void close() throws IllegalWindowMove {
        if (!this.isOpen) throw new IllegalWindowMove(this + " уже закрыто!");
        this.isOpen = false;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public String toString() {
        return "Окно";
    }
}
