package moves.status;

import ru.ifmo.se.pokemon.*;

/**
 * Confide lowers the target's Special Attack by one stage.
 */

public class Confide extends StatusMove {
    public Confide() {
        super(Type.NORMAL, 0, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.ATTACK, -1);
    }

    @Override
    protected String describe() {
        return "использует Confide";
    }
}
