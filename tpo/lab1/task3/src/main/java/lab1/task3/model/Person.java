package lab1.task3.model;

import java.util.Objects;

public class Person {

    private final String name;
    private boolean arrested;
    private boolean lockedUp;

    public Person(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getName() {
        return name;
    }

    public boolean isArrested() {
        return arrested;
    }

    public void setArrested(boolean arrested) {
        this.arrested = arrested;
    }

    public boolean isLockedUp() {
        return lockedUp;
    }

    public void setLockedUp(boolean lockedUp) {
        this.lockedUp = lockedUp;
    }

    @Override
    public String toString() {
        return name;
    }
}
