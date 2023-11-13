package creatures;

public class Moomin extends Creature {
    public Moomin(){
        super();
    }
    public Moomin(String name) {
        super(name);
    }

    @Override
    public String describe() {
        return "Муми-тролль по имени " + getName() + " едет " + moveType + ".";
    }
}
