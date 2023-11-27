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
        this.name = "Default name";
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

    public void rotate() {
        setStatus(Status.ROTATING);
        this.rotationSpeed += 1;
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

    public void move(MoveType type) {
        setStatus(Status.MOVING);
        setMoveType(type);
    }

    public abstract String describe();

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rotationSpeed, status, moveType);
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return hashCode() == o.hashCode();
    }
}
