package pokemons;

import moves.status.Sing;
import moves.status.Swagger;
import ru.ifmo.se.pokemon.*;

public class Igglybuff extends Pokemon {
    public Igglybuff(String name, int level) {
        super(name, level);
        setType(Type.NORMAL, Type.FAIRY);
        setStats(90, 30, 15, 40, 20, 15);
        setMove(new Sing(), new Swagger());
    }

}
