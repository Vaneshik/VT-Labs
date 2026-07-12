package lab1.task3.command;

import lab1.task3.model.Person;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ArrestCommandTest {

    @Test
    void executeSetsArrestedFlag() {
        Person p = new Person("Виит Вужагиг");
        new ArrestCommand(p).execute();
        assertTrue(p.isArrested());
    }

    @Test
    void executePrintsPersonName() {
        Person p = new Person("Виит Вужагиг");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        new ArrestCommand(p).execute();
        System.setOut(System.out);
        assertTrue(out.toString().contains("Виит Вужагиг"));
    }

    @Test
    void nullTargetThrows() {
        assertThrows(NullPointerException.class, () -> new ArrestCommand(null));
    }
}
