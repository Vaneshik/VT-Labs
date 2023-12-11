package lab4.room.items;

import lab4.enums.LiquidType;
import lab4.enums.Status;
import lab4.interfaces.Spillable;

abstract public class LiquidContainer extends Container implements Spillable {
    private LiquidType liquidType;

    public LiquidContainer() {
        this.setLiquidType(LiquidType.DEFAULT);
    }

    public LiquidContainer(LiquidType liquid) {
        this.setLiquidType(liquid);
    }

    public LiquidType getLiquidType() {
        return liquidType;
    }

    public void setLiquidType(LiquidType liquid) {
        this.liquidType = liquid;
    }

    abstract public boolean checkIfSpilling(int speed);

    public void spill() {
        setStatus(Status.SPILLING);
    }

    public String toString() {
        return "LiquidContainer";
    }
}
