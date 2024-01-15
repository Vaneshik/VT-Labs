package creatures;

import enums.MoveType;
import enums.Status;
import interfaces.Movable;
import interfaces.Rotatable;

import java.util.Objects;

public abstract class Creature implements Movable, Rotatable {
    private String name;
    private int rotationSpeed = 0;
    private Status status = Status.DEFAULT;
    private MoveType moveType = MoveType.DEFAULT;

    public Creature() {
        this.name = "ноунейм";
    }

    public Creature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
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
        return "Существо по имени " + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }

        return ((Creature) o).name.equals(this.name) &&
                ((Creature) o).rotationSpeed == this.rotationSpeed &&
                ((Creature) o).status == this.status &&
                ((Creature) o).moveType == this.moveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rotationSpeed, status, moveType);
    }
}
