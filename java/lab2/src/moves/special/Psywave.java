package moves.special;

import ru.ifmo.se.pokemon.*;

/**
 * Psywave inflicts a random amount of HP damage, varying between 50% and 150% of the defender's level.
 * In other words, at level 100 the damage will be 50-150 HP.
 */

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
