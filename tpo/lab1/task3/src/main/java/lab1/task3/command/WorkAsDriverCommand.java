package lab1.task3.command;

import lab1.task3.model.PenFamily;
import lab1.task3.model.Person;

import java.util.Objects;

public class WorkAsDriverCommand implements Command {

    private final Person driver;
    private final PenFamily employer;

    public WorkAsDriverCommand(Person driver, PenFamily employer) {
        this.driver = Objects.requireNonNull(driver, "driver");
        this.employer = Objects.requireNonNull(employer, "employer");
    }

    @Override
    public void execute() {
        employer.hire(driver);
        System.out.println(driver.getName() + " работал шофёром лимузина у " + employer.getDescription());
    }
}
