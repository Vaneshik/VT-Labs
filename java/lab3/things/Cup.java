package things;

import enums.Status;
import interfaces.Spillable;

public class Cup extends Thing implements Spillable {

    @Override
    public void spill() {
        setStatus(Status.SPILLING);
    }

    public void checkSpeed(int speed) {
        if (speed > 2) {
            this.spill();
            System.out.println("Чай выплескивается из чашки.");
        } else {
            System.out.println("Чай в полном порядке.");
        }
    }

    @Override
    public String describe() {
        return "Кружка с чаем.";
    }
}
