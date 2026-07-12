package lab1.task3.command;

import lab1.task3.model.Person;
import lab1.task3.model.Planet;

import java.util.Objects;

public class ClaimDiscoveryCommand implements Command {

    private final Person claimant;
    private final Planet planet;

    public ClaimDiscoveryCommand(Person claimant, Planet planet) {
        this.claimant = Objects.requireNonNull(claimant, "claimant");
        this.planet = Objects.requireNonNull(planet, "planet");
    }

    @Override
    public void execute() {
        planet.setDiscovered(true);
        System.out.println(claimant.getName() + " заявил, что нашёл планету " + planet.getName());
    }
}
