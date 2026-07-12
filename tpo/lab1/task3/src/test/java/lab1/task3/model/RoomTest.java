package lab1.task3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void lockedRoomIsLocked() {
        Room room = new Room("запертая комната", true);
        assertTrue(room.isLocked());
    }

    @Test
    void openRoomIsNotLocked() {
        Room room = new Room("открытая комната", false);
        assertFalse(room.isLocked());
    }

    @Test
    void initiallyHasNoBooks() {
        Room room = new Room("запертая комната", true);
        assertTrue(room.getBooks().isEmpty());
    }

    @Test
    void addedBookAppearsInList() {
        Room room = new Room("запертая комната", true);
        Person author = new Person("Виит Вужагиг");
        Book book = new Book("Теория планет", author);
        room.addBook(book);
        assertEquals(1, room.getBooks().size());
        assertSame(book, room.getBooks().get(0));
    }

    @Test
    void addNullBookThrows() {
        Room room = new Room("запертая комната", true);
        assertThrows(NullPointerException.class, () -> room.addBook(null));
    }

    @Test
    void nullDescriptionThrows() {
        assertThrows(NullPointerException.class, () -> new Room(null, true));
    }
}
