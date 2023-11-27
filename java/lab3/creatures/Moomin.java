package creatures;

import enums.MoveType;

public class Moomin extends Creature {
    public Moomin() {
        super();
    }

    public Moomin(String name) {
        super(name);
    }

    @Override
    public String describe() {
        return "Муми-тролль по имени " + getName();
    }

    @Override
    public void move(MoveType type) {
        super.move(type);
        System.out.println(describe() + " едет " + getMoveType() + ".");
    }
}
