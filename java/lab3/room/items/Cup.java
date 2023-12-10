package room.items;

import enums.LiquidType;

public class Cup extends LiquidContainer {
    public Cup() {
        super();
    }

    public Cup(LiquidType liquid) {
        super(liquid);
    }

    @Override
    public boolean checkIfSpilling(int speed) {
        if (speed > 2) {
            this.spill();
            System.out.println(this.getLiquidType() + " " + this.getStatus() + " из " + this + ".");
            return true;
        } else {
            System.out.println(this.getLiquidType() + " в полном порядке.");
            return false;
        }
    }

    @Override
    public String toString() {
        return "Чашка";
    }
}
