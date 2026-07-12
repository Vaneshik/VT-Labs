package lab1.task3.command;

import lab1.task3.model.Person;
import lab1.task3.model.Planet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisitPlanetCommandTest {

    @Test
    void executeSetsVisitedFlag() {
        Person p = new Person("Виит Вужагиг");
        Planet planet = new Planet("Неизвестная планета");
        new VisitPlanetCommand(p, planet).execute();
        assertTrue(planet.isVisited());
    }

    @Test
    void discoveredFlagNotAffected() {
        Planet planet = new Planet("Неизвестная планета");
        assertFalse(planet.isDiscovered());
        new VisitPlanetCommand(new Person("x"), planet).execute();
        assertFalse(planet.isDiscovered());
    }

    @Test
    void nullVisitorThrows() {
        assertThrows(NullPointerException.class,
                () -> new VisitPlanetCommand(null, new Planet("x")));
    }

    @Test
    void nullPlanetThrows() {
        assertThrows(NullPointerException.class,
                () -> new VisitPlanetCommand(new Person("x"), null));
    }
}
