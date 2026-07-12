package lab1.task3.command;

import lab1.task3.model.PenFamily;
import lab1.task3.model.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkAsDriverCommandTest {

    @Test
    void executeHiresDriver() {
        Person driver = new Person("Виит Вужагиг");
        PenFamily employer = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        new WorkAsDriverCommand(driver, employer).execute();
        assertTrue(employer.employs(driver));
    }

    @Test
    void nullDriverThrows() {
        assertThrows(NullPointerException.class,
                () -> new WorkAsDriverCommand(null, new PenFamily("x")));
    }

    @Test
    void nullEmployerThrows() {
        assertThrows(NullPointerException.class,
                () -> new WorkAsDriverCommand(new Person("x"), null));
    }
}
