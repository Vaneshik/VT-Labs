package room;

import creatures.Moomin;
import exceptions.IllegalMoominsCount;
import room.furniture.Window;
import room.items.RoomItem;

import java.util.Vector;

public class Room {
    private Vector<Moomin> moomins = new Vector<Moomin>();
    private Vector<RoomItem> roomItems = new Vector<RoomItem>();

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

    public void addMoomin(Moomin moomin) {
        this.moomins.add(moomin);

        if (this.moomins.size() > 3) {
            throw new IllegalMoominsCount();
        }
    }

    public Vector<RoomItem> getRoomItems() {
        return roomItems;
    }

    public void setRoomItems(Vector<RoomItem> roomItems) {
        this.roomItems = roomItems;
    }

    public void addRoomItem(RoomItem roomItem) {
        this.roomItems.add(roomItem);
    }
}
