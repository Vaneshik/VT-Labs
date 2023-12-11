package lab4.room;

import lab4.enums.Status;
import lab4.interfaces.Rotatable;
import lab4.room.items.RoomItem;
import java.util.Objects;

public class Floor extends RoomItem implements Rotatable {
    private int rotationSpeed = 0;

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
        return "Пол";
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o) &&
                ((Floor) o).rotationSpeed == this.rotationSpeed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rotationSpeed);
    }
}
