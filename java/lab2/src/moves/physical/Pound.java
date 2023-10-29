package moves.physical;

import ru.ifmo.se.pokemon.*;

/**
 * Pound deals damage with no additional effect.
 */

public class Pound extends PhysicalMove {
    public Pound() {
        super(Type.NORMAL, 40, 100);
    }

    @Override
    protected String describe() {
        return "использует Pound";
    }
}
