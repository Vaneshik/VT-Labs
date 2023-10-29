package pokemons;

import moves.physical.Bulldoze;
import moves.status.Confide;
import moves.physical.StoneEdge;
import moves.physical.VitalThrow;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

public class Pheromosa extends Pokemon {
    public Pheromosa(String name, int level) {
        super(name, level);
        setType(Type.BUG, Type.FIGHTING);
        setStats(71, 137, 37, 137, 37, 151);
        setMove(new VitalThrow(), new Confide(), new Bulldoze(), new StoneEdge());
    }
}
