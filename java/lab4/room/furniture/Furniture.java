package room.furniture;

import enums.MoveType;
import enums.Status;
import interfaces.Movable;
import interfaces.Rotatable;
import room.items.RoomItem;

import java.util.Objects;

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
        System.out.println(this + " " + this.getStatus() + " " + this.getMoveType() + ".");
    }

    public void rotate() {
        setStatus(Status.ROTATING);
        rotationSpeed += 1;
        System.out.println(this + " начал медленно вращаться.");
    }

    public int getRotationSpeed() {
        return rotationSpeed;
    }

    public void speedUp(int power) {
        if (this.getStatus() == Status.ROTATING) {
            this.rotationSpeed += power;
            System.out.println(this + " стал вращаться быстрее.");
        }
    }

    @Override
    public String toString() {
        return "Мебель";
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }

        return super.equals(o) &&
                ((Furniture) o).moveType == this.moveType &&
                ((Furniture) o).rotationSpeed == this.rotationSpeed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), moveType, rotationSpeed);
    }
}
