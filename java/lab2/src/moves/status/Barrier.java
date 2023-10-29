package moves.status;

import ru.ifmo.se.pokemon.*;

/**
 * Barrier raises the user's Defense by two stages.
 */

public class Barrier extends StatusMove {
    public Barrier() {
        super(Type.PSYCHIC, 0, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.DEFENSE, 2);
    }

    @Override
    protected String describe() {
        return "использует Barrier";
    }
}
