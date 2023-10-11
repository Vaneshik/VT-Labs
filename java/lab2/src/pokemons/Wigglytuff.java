package pokemons;

import moves.physical.DoubleSlap;
import moves.physical.Pound;
import moves.status.Sing;
import moves.status.Swagger;
import ru.ifmo.se.pokemon.*;

public class Wigglytuff extends Jigglypuff {
    public Wigglytuff(String name, int level) {
        super(name, level);
        setType(Type.NORMAL, Type.FAIRY);
        setStats(140, 70, 45, 85, 50, 45);
        setMove(new Sing(), new Swagger(), new Pound(), new DoubleSlap());
    }

}
