package lab1.task3.command;

import lab1.task3.model.Book;
import lab1.task3.model.Person;
import lab1.task3.model.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WriteBookCommandTest {

    @Test
    void executeAddsBookToRoom() {
        Person author = new Person("Виит Вужагиг");
        author.setLockedUp(true);
        Room room = new Room("запертая комната", true);
        Book book = new Book("Теория планет", author);
        new WriteBookCommand(author, book, room).execute();
        assertEquals(1, room.getBooks().size());
        assertSame(book, room.getBooks().get(0));
    }

    @Test
    void executeWhenNotLockedUpThrows() {
        Person author = new Person("Виит Вужагиг");
        Room room = new Room("запертая комната", true);
        Book book = new Book("Теория планет", author);
        assertThrows(IllegalStateException.class,
                () -> new WriteBookCommand(author, book, room).execute());
    }

    @Test
    void nullAuthorThrows() {
        Room room = new Room("x", true);
        Person p = new Person("x");
        Book book = new Book("x", p);
        assertThrows(NullPointerException.class, () -> new WriteBookCommand(null, book, room));
    }

    @Test
    void nullBookThrows() {
        Person p = new Person("x");
        Room room = new Room("x", true);
        assertThrows(NullPointerException.class, () -> new WriteBookCommand(p, null, room));
    }

    @Test
    void nullRoomThrows() {
        Person p = new Person("x");
        Book book = new Book("x", p);
        assertThrows(NullPointerException.class, () -> new WriteBookCommand(p, book, null));
    }
}
