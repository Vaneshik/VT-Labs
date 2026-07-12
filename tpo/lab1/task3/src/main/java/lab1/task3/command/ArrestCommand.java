package lab1.task3.command;

import lab1.task3.model.Person;

import java.util.Objects;

public class ArrestCommand implements Command {

    private final Person target;

    public ArrestCommand(Person target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public void execute() {
        target.setArrested(true);
        System.out.println("За " + target.getName() + " приехали");
    }
}
