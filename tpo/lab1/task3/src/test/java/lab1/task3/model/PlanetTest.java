package lab1.task3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanetTest {

    @Test
    void createdWithName() {
        Planet p = new Planet("Неизвестная планета");
        assertEquals("Неизвестная планета", p.getName());
    }

    @Test
    void initiallyNotDiscovered() {
        Planet p = new Planet("Неизвестная планета");
        assertFalse(p.isDiscovered());
    }

    @Test
    void initiallyNotVisited() {
        Planet p = new Planet("Неизвестная планета");
        assertFalse(p.isVisited());
    }

    @Test
    void canBeDiscovered() {
        Planet p = new Planet("Неизвестная планета");
        p.setDiscovered(true);
        assertTrue(p.isDiscovered());
    }

    @Test
    void canBeVisited() {
        Planet p = new Planet("Неизвестная планета");
        p.setVisited(true);
        assertTrue(p.isVisited());
    }

    @Test
    void nullNameThrows() {
        assertThrows(NullPointerException.class, () -> new Planet(null));
    }
}
