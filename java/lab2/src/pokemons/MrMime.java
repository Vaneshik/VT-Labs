package pokemons;

import moves.physical.WakeUpSlap;
import moves.special.IcyWind;
import moves.special.Psywave;
import moves.status.Barrier;
import ru.ifmo.se.pokemon.*;

public class MrMime extends MimeJr {
    public MrMime(String name, int level) {
        super(name, level);
        setStats(40, 45, 65, 100, 120, 90);
        addMove(new Psywave());
    }
}
