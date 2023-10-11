package moves.special;

import ru.ifmo.se.pokemon.*;

public class Psywave extends SpecialMove {
    public Psywave() {
        super(Type.PSYCHIC, 0, 100);
    }

    @Override
    protected void applyOppDamage(Pokemon p, double att) {
        super.applyOppDamage(p, p.getLevel() * (Math.random() + 0.5));
    }

    @Override
    protected String describe() {
        return "использует Psywave";
    }
}
