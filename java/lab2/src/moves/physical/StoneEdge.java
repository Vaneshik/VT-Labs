package moves.physical;

import ru.ifmo.se.pokemon.*;

/**
 * Stone Edge deals damage and has an increased critical hit ratio (1⁄8 instead of 1⁄24).
 */

public class StoneEdge extends PhysicalMove {
    public StoneEdge() {
        super(Type.ROCK, 100, 80);
    }

    @Override
    protected double calcCriticalHit(Pokemon p, Pokemon p1) {
        if (3 * p.getStat(Stat.SPEED) / 512.0 > Math.random()) {
            return 2.0;
        } else {
            return 1.0;
        }
    }

    @Override
    protected String describe() {
        return "использует Stone Edge";
    }
}
