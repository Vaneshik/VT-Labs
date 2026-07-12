package lab1.task3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PenFamily {

    private final String description;
    private final List<Person> employees = new ArrayList<>();

    public PenFamily(String description) {
        this.description = Objects.requireNonNull(description, "description");
    }

    public String getDescription() {
        return description;
    }

    public void hire(Person person) {
        employees.add(Objects.requireNonNull(person, "person"));
    }

    public boolean employs(Person person) {
        return employees.contains(person);
    }

    public List<Person> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    @Override
    public String toString() {
        return description;
    }
}
