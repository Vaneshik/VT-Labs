package moves.physical;

import ru.ifmo.se.pokemon.*;

public class WakeUpSlap extends PhysicalMove {
    public WakeUpSlap() {
        super(Type.FIGHTING, 70, 100);
    }

    @Override
    protected void applyOppDamage(Pokemon p, double att) {
        super.applyOppDamage(p, p.getCondition() == Status.SLEEP ? att * 2 : att);
    }

    @Override
    protected String describe() {
        return "использует Wake-Up Slap";
    }
}
