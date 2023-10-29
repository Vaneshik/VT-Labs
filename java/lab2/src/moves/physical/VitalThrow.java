package moves.physical;

import ru.ifmo.se.pokemon.*;

/**
 * Vital Throw deals damage with lower priority, so most attacks will take place before it.
 * Vital Throw ignores changes to the Accuracy and Evasion stats.
 */

public class VitalThrow extends PhysicalMove {
    public VitalThrow() {
        super(Type.FIGHTING, 70, Double.POSITIVE_INFINITY, -1, 0);
    }

    @Override
    protected String describe() {
        return "использует Vital Throw";
    }
}
