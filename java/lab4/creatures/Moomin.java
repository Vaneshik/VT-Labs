package creatures;

public class Moomin extends Creature {
    public Moomin() {
        super();
    }

    public Moomin(String name) {
        super(name);
    }

    public void lookOut() {
        System.out.println(this + " выглянул.");
    }

    @Override
    public String toString() {
        return "Муми-тролль по имени " + getName();
    }
}
