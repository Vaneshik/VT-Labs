package lab4.creatures;

public class Moomin extends Creature {
    public Moomin() {
        super();
    }

    public Moomin(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Муми-тролль по имени " + getName();
    }
}
