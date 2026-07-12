package lab1.task3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void createdWithName() {
        Person p = new Person("Виит Вужагиг");
        assertEquals("Виит Вужагиг", p.getName());
    }

    @Test
    void initiallyNotArrested() {
        Person p = new Person("Виит Вужагиг");
        assertFalse(p.isArrested());
    }

    @Test
    void initiallyNotLockedUp() {
        Person p = new Person("Виит Вужагиг");
        assertFalse(p.isLockedUp());
    }

    @Test
    void canBeArrested() {
        Person p = new Person("Виит Вужагиг");
        p.setArrested(true);
        assertTrue(p.isArrested());
    }

    @Test
    void canBeLockedUp() {
        Person p = new Person("Виит Вужагиг");
        p.setLockedUp(true);
        assertTrue(p.isLockedUp());
    }

    @Test
    void nullNameThrows() {
        assertThrows(NullPointerException.class, () -> new Person(null));
    }

    @Test
    void toStringReturnsName() {
        Person p = new Person("Виит Вужагиг");
        assertEquals("Виит Вужагиг", p.toString());
    }
}
