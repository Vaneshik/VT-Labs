package room;

import enums.MoveType;

public class Chair extends Furniture {
    @Override
    public void move(MoveType type) {
        super.move(type);
        System.out.println("Стул едет " + getMoveType() + ".");
    }

    @Override
    public String describe() {
        return "Стул";
    }
}
