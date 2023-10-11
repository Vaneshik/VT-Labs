package moves.status;

import ru.ifmo.se.pokemon.*;

public class Sing extends StatusMove {
    public Sing() {
        super(Type.NORMAL, 0, 85);
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
