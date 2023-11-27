package room;

import enums.MoveType;

public class Wardrobe extends Furniture {

    @Override
    public void move(MoveType type) {
        super.move(type);
        System.out.println("Плятяной шкаф едет " + getMoveType() + ".");
    }

    @Override
    public String describe() {
        return "Шкаф.";
    }
}
