package moves.physical;

import ru.ifmo.se.pokemon.PhysicalMove;
import ru.ifmo.se.pokemon.Type;

public class DoubleSlap extends PhysicalMove {
    public DoubleSlap() {
        super(Type.NORMAL, 15, 85);
    }

    @Override
    protected String describe() {
        return "использует Double Slap";
    }
}
