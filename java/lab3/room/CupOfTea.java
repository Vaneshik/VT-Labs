package room;

public class CupOfTea extends Cup {
    @Override
    public void checkIfSpilling(int speed) {
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
