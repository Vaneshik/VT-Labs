package lab1.task3.command;

import lab1.task3.model.Person;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class SendAwayCommandTest {

    @Test
    void executePrintsPersonAndDestination() {
        Person p = new Person("Виит Вужагиг");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        new SendAwayCommand(p, "где люди не платят налогов").execute();
        System.setOut(System.out);
        String output = out.toString();
        assertTrue(output.contains("Виит Вужагиг"));
        assertTrue(output.contains("где люди не платят налогов"));
    }

    @Test
    void nullPersonThrows() {
        assertThrows(NullPointerException.class,
                () -> new SendAwayCommand(null, "x"));
    }

    @Test
    void nullDestinationThrows() {
        assertThrows(NullPointerException.class,
                () -> new SendAwayCommand(new Person("x"), null));
    }
}
