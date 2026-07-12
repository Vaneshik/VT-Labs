package lab1.task3.model;

import java.util.Objects;

public class Book {

    private final String title;
    private final Person author;

    public Book(String title, Person author) {
        this.title = Objects.requireNonNull(title, "title");
        this.author = Objects.requireNonNull(author, "author");
    }

    public String getTitle() {
        return title;
    }

    public Person getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "\"" + title + "\" (" + author.getName() + ")";
    }
}
