package room;

import creatures.Moomin;
import exceptions.IllegalMoominsCount;
import room.furniture.Window;

import java.util.Vector;

public class Room {
    private Vector<Moomin> moomins = new Vector<>();

    final private Window window = new Window();
    private final Floor floor = new Floor();

    public Floor getFloor() {
        return floor;
    }

    public Vector<Moomin> getMoomins() {
        return moomins;
    }

    public void setMoomins(Vector<Moomin> moomins) {
        this.moomins = moomins;
    }

    public Window getWindow() {
        return window;
    }

    public void addMoomins(Moomin moomin) {
        this.moomins.add(moomin);

        if (this.moomins.size() > 3) {
            throw new IllegalMoominsCount();
        }
        ;
    }
}
