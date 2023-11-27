package room;

import enums.MoveType;
public class Trellie extends Furniture {
    @Override
    public void move(MoveType type) {
        super.move(type);
        System.out.println("Трельяж едет " + getMoveType() + ".");
    }

    @Override
    public String describe() {
        return "Трельяж";
    }
}
