package room;

import enums.Status;
import interfaces.Spillable;

abstract public class Cup extends Furniture implements Spillable {

    public void spill() {
        setStatus(Status.SPILLING);
    }

    abstract public void checkIfSpilling(int speed);
}
