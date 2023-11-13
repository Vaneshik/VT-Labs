package things;

import enums.Status;
import interfaces.Rotatable;

public class Floor extends Thing implements Rotatable {
    private int rotationSpeed = 0;

    @Override
    public void rotate() {
        setStatus(Status.ROTATING);
        rotationSpeed += 1;
        System.out.println("Пол начал медленно вращаться.");
    }

    @Override
    public int getRotationSpeed() {
        return rotationSpeed;
    }

    @Override
    public void speedUp(int power) {
        if (this.getStatus() == Status.ROTATING) {
            this.rotationSpeed += power;
            System.out.println("Пол стал вращаться быстрее.");
        }
    }

    @Override
    public String describe() {
        return "Стол.";
    }
}
