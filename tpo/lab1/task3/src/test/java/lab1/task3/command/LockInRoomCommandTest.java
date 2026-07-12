package lab1.task3.command;

import lab1.task3.model.Person;
import lab1.task3.model.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LockInRoomCommandTest {

    @Test
    void executeSetsLockedUpFlag() {
        Person p = new Person("Виит Вужагиг");
        Room room = new Room("запертая комната", true);
        new LockInRoomCommand(p, room).execute();
        assertTrue(p.isLockedUp());
    }

    @Test
    void executeInUnlockedRoomThrows() {
        Person p = new Person("Виит Вужагиг");
        Room room = new Room("открытая комната", false);
        assertThrows(IllegalStateException.class, () -> new LockInRoomCommand(p, room).execute());
    }

    @Test
    void nullPersonThrows() {
        assertThrows(NullPointerException.class,
                () -> new LockInRoomCommand(null, new Room("x", true)));
    }

    @Test
    void nullRoomThrows() {
        assertThrows(NullPointerException.class,
                () -> new LockInRoomCommand(new Person("x"), null));
    }
}
