package room;

import enums.MoveType;
import enums.Status;
import interfaces.Movable;
import interfaces.Rotatable;

public abstract class Furniture extends RoomItem implements Movable, Rotatable {
    private MoveType moveType = MoveType.DEFAULT;
    private int rotationSpeed = 0;

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void move(MoveType type) {
        setStatus(Status.MOVING);
        setMoveType(type);
    }

    public void rotate() {
        setStatus(Status.ROTATING);
        rotationSpeed += 1;
        System.out.println("Пол начал медленно вращаться.");
    }

    public int getRotationSpeed() {
        return rotationSpeed;
    }

    public void speedUp(int power) {
        if (this.getStatus() == Status.ROTATING) {
            this.rotationSpeed += power;
            System.out.println("Пол стал вращаться быстрее.");
        }
    }
}