package pokemons;

import moves.physical.Pound;
import moves.status.Sing;
import moves.status.Swagger;
import ru.ifmo.se.pokemon.*;

public class Jigglypuff extends Igglybuff {
    public Jigglypuff(String name, int level) {
        super(name, level);
        setStats(115, 45, 20, 45, 35, 20);
        addMove(new Pound());
    }

}
