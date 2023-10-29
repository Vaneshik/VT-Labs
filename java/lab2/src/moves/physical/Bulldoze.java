package moves.physical;

import ru.ifmo.se.pokemon.*;

/**
 * Bulldoze deals damage and lowers the target's Speed by one stage.
 */

public class Bulldoze extends PhysicalMove {
    public Bulldoze() {
        super(Type.GROUND, 60, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.ATTACK, -1);
    }

    @Override
    protected String describe() {
        return "использует Bulldoze";
    }
}
