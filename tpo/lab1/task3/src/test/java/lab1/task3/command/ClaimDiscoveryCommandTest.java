package lab1.task3.command;

import lab1.task3.model.Person;
import lab1.task3.model.Planet;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ClaimDiscoveryCommandTest {

    @Test
    void executeSetsDiscoveredFlag() {
        Person p = new Person("Виит Вужагиг");
        Planet planet = new Planet("Неизвестная планета");
        new ClaimDiscoveryCommand(p, planet).execute();
        assertTrue(planet.isDiscovered());
    }

    @Test
    void executePrintsMessage() {
        Person p = new Person("Виит Вужагиг");
        Planet planet = new Planet("Неизвестная планета");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        new ClaimDiscoveryCommand(p, planet).execute();
        System.setOut(System.out);

        String output = out.toString();
        assertTrue(output.contains("Виит Вужагиг"));
        assertTrue(output.contains("Неизвестная планета"));
    }

    @Test
    void nullClaimantThrows() {
        assertThrows(NullPointerException.class,
                () -> new ClaimDiscoveryCommand(null, new Planet("x")));
    }

    @Test
    void nullPlanetThrows() {
        assertThrows(NullPointerException.class,
                () -> new ClaimDiscoveryCommand(new Person("x"), null));
    }
}
