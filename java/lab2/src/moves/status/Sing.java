package moves.status;

import ru.ifmo.se.pokemon.*;

/**
 * Sing puts the target to sleep, if it hits.
 */

public class Sing extends StatusMove {
    public Sing() {
        super(Type.NORMAL, 0, 55);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        Effect.sleep(p);
    }

    @Override
    protected String describe() {
        return "использует Sing";
    }
}
