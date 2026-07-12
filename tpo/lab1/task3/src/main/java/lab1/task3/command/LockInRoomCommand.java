package lab1.task3.command;

import lab1.task3.model.Person;
import lab1.task3.model.Room;

import java.util.Objects;

public class LockInRoomCommand implements Command {

    private final Person person;
    private final Room room;

    public LockInRoomCommand(Person person, Room room) {
        this.person = Objects.requireNonNull(person, "person");
        this.room = Objects.requireNonNull(room, "room");
    }

    @Override
    public void execute() {
        if (!room.isLocked()) {
            throw new IllegalStateException("Комната не заперта");
        }
        person.setLockedUp(true);
        System.out.println(person.getName() + " помещён в " + room.getDescription());
    }
}
