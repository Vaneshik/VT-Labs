package things;

import enums.MoveType;
import enums.Status;
import interfaces.Movable;

public abstract class MovableThing extends Thing implements Movable {
    protected MoveType moveType = MoveType.DEFAULT;

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    @Override
    public void move(MoveType type) {
        setStatus(Status.MOVING);
        setMoveType(type);
    }
}
