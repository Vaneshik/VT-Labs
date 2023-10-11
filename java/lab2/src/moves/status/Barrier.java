package moves.status;

import ru.ifmo.se.pokemon.*;

public class Barrier extends StatusMove {
    public Barrier() {
        super(Type.PSYCHIC, 0, 0);
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
