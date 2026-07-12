package ru.ifmo.person.transaction;

import ru.ifmo.person.model.ImportHistory;
import ru.ifmo.person.model.Person;

import java.util.List;

public class ImportResult {
    private final ImportHistory history;
    private final List<Person> persons;

    public ImportResult(ImportHistory history, List<Person> persons) {
        this.history = history;
        this.persons = persons;
    }

    public ImportHistory getHistory() {
        return history;
    }

    public List<Person> getPersons() {
        return persons;
    }
}
