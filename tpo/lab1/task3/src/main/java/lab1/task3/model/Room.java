package lab1.task3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Room {

    private final String description;
    private final boolean locked;
    private final List<Book> books = new ArrayList<>();

    public Room(String description, boolean locked) {
        this.description = Objects.requireNonNull(description, "description");
        this.locked = locked;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLocked() {
        return locked;
    }

    public void addBook(Book book) {
        books.add(Objects.requireNonNull(book, "book"));
    }

    public List<Book> getBooks() {
        return Collections.unmodifiableList(books);
    }

    @Override
    public String toString() {
        return description;
    }
}
