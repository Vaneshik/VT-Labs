package room;

import exceptions.IllegalWindowMove;

public class Window {
    private boolean isOpen = false;

    public void open() throws IllegalWindowMove {
        if (this.isOpen) throw new IllegalWindowMove(this + " уже открыто!", this);
        this.isOpen = true;
    }

    public void close() throws IllegalWindowMove {
        if (!this.isOpen) throw new IllegalWindowMove(this + " уже закрыто!", this);
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
