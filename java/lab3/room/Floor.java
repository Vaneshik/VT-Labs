package room;

import enums.Status;
import interfaces.Rotatable;

import java.util.Objects;

public class Floor extends RoomItem implements Rotatable {
    private int rotationSpeed = 0;

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

    public String describe() {
        return "Пол.";
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rotationSpeed);
    }
}
