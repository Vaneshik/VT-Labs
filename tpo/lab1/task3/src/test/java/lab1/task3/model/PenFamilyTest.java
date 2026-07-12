package lab1.task3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PenFamilyTest {

    @Test
    void createdWithDescription() {
        PenFamily pf = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        assertEquals("семья дешёвых зелёных авторучек на пружинках", pf.getDescription());
    }

    @Test
    void initiallyHasNoEmployees() {
        PenFamily pf = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        assertTrue(pf.getEmployees().isEmpty());
    }

    @Test
    void hiredPersonIsEmployee() {
        PenFamily pf = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        Person driver = new Person("Виит Вужагиг");
        pf.hire(driver);
        assertTrue(pf.employs(driver));
    }

    @Test
    void notHiredPersonIsNotEmployee() {
        PenFamily pf = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        Person driver = new Person("Виит Вужагиг");
        assertFalse(pf.employs(driver));
    }

    @Test
    void hireNullThrows() {
        PenFamily pf = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        assertThrows(NullPointerException.class, () -> pf.hire(null));
    }
}
