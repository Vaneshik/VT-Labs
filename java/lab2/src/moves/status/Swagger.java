package moves.status;

import ru.ifmo.se.pokemon.*;

/**
 * Swagger confuses the target and raises its Attack by two stages
 */

public class Swagger extends StatusMove {
    public Swagger() {
        super(Type.NORMAL, 0, 85);
    }

    protected void applyOppEffects(Pokemon p) {
        Effect.confuse(p);
        p.setMod(Stat.ATTACK, 2);
    }

    @Override
    protected String describe() {
        return "использует Swagger";
    }
}
