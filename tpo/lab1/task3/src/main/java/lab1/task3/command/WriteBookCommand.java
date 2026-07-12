package lab1.task3.command;

import lab1.task3.model.Book;
import lab1.task3.model.Person;
import lab1.task3.model.Room;

import java.util.Objects;

public class WriteBookCommand implements Command {

    private final Person author;
    private final Book book;
    private final Room room;

    public WriteBookCommand(Person author, Book book, Room room) {
        this.author = Objects.requireNonNull(author, "author");
        this.book = Objects.requireNonNull(book, "book");
        this.room = Objects.requireNonNull(room, "room");
    }

    @Override
    public void execute() {
        if (!author.isLockedUp()) {
            throw new IllegalStateException(author.getName() + " не находится в комнате");
        }
        room.addBook(book);
        System.out.println(author.getName() + " написал книгу " + book);
    }
}
