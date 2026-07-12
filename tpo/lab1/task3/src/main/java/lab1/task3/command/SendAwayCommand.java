package lab1.task3.command;

import lab1.task3.model.Person;

import java.util.Objects;

public class SendAwayCommand implements Command {

    private final Person person;
    private final String destination;

    public SendAwayCommand(Person person, String destination) {
        this.person = Objects.requireNonNull(person, "person");
        this.destination = Objects.requireNonNull(destination, "destination");
    }

    @Override
    public void execute() {
        System.out.println(person.getName() + " отправлен туда, " + destination);
    }
}
