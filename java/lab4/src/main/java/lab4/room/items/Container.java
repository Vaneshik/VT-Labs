package lab4.room.items;

import lab4.enums.MoveType;
import lab4.enums.Status;
import java.util.Objects;

abstract public class Container extends RoomItem {
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
        return "Контейнер";
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o) &&
                ((Container) o).moveType == this.moveType &&
                ((Container) o).rotationSpeed == this.rotationSpeed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), moveType, rotationSpeed);
    }
}
