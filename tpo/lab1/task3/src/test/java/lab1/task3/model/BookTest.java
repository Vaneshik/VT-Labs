package lab1.task3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void bookHasTitleAndAuthor() {
        Person author = new Person("Виит Вужагиг");
        Book book = new Book("Теория планет", author);
        assertEquals("Теория планет", book.getTitle());
        assertSame(author, book.getAuthor());
    }

    @Test
    void nullTitleThrows() {
        Person author = new Person("Виит Вужагиг");
        assertThrows(NullPointerException.class, () -> new Book(null, author));
    }

    @Test
    void nullAuthorThrows() {
        assertThrows(NullPointerException.class, () -> new Book("Теория планет", null));
    }

    @Test
    void toStringContainsTitleAndAuthor() {
        Person author = new Person("Виит Вужагиг");
        Book book = new Book("Теория планет", author);
        String str = book.toString();
        assertTrue(str.contains("Теория планет"));
        assertTrue(str.contains("Виит Вужагиг"));
    }
}
