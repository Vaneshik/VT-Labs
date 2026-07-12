package lab1.task3.command;

import lab1.task3.model.Person;
import lab1.task3.model.Planet;

import java.util.Objects;

public class VisitPlanetCommand implements Command {

    private final Person visitor;
    private final Planet planet;

    public VisitPlanetCommand(Person visitor, Planet planet) {
        this.visitor = Objects.requireNonNull(visitor, "visitor");
        this.planet = Objects.requireNonNull(planet, "planet");
    }

    @Override
    public void execute() {
        planet.setVisited(true);
        System.out.println(visitor.getName() + " побывал на планете " + planet.getName());
    }
}
