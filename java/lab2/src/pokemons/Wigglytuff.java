package pokemons;

import moves.physical.DoubleSlap;
import moves.physical.Pound;
import moves.status.Sing;
import moves.status.Swagger;
import ru.ifmo.se.pokemon.*;

public class Wigglytuff extends Jigglypuff {
    public Wigglytuff(String name, int level) {
        super(name, level);
        setStats(140, 70, 45, 85, 50, 45);
        addMove(new DoubleSlap());
    }

}
