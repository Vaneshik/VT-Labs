package room;

import enums.MoveType;

public class Table extends Furniture {
    @Override
    public void move(MoveType type) {
        super.move(type);
        System.out.println("Стол едет " + getMoveType() + ".");
    }

    @Override
    public String describe() {
        return "Стол.";
    }
}
