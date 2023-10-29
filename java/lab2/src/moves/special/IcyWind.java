package moves.special;

import ru.ifmo.se.pokemon.*;

/**
 * Icy Wind deals damage and lowers the target's Speed by one stage.
 */

public class IcyWind extends SpecialMove {
    public IcyWind() {
        super(Type.ICE, 55, 95);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.SPEED, -1);
    }

    @Override
    protected String describe() {
        return "использует IcyWind";
    }
}
