package moves.physical;

import ru.ifmo.se.pokemon.*;

/**
 * Wake-Up Slap deals damage, and hits with double power (140) if the target is asleep.
 * However, it also wakes up the target.
 */

public class WakeUpSlap extends PhysicalMove {
    public WakeUpSlap() {
        super(Type.FIGHTING, 70, 100);
    }

    @Override
    protected double calcBaseDamage(Pokemon att, Pokemon def) {
        if (def.getCondition() == Status.SLEEP) {
            def.setCondition(new Effect());
            return 2 * super.calcBaseDamage(att, def);
        } else {
            return super.calcBaseDamage(att, def);
        }
    }

    @Override
    protected String describe() {
        return "использует Wake-Up Slap";
    }
}
