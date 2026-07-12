package lab1.task3;

import lab1.task3.command.*;
import lab1.task3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    private Person vuit;
    private Planet planet;
    private PenFamily penFamily;
    private Room room;
    private Book book;
    private Scenario scenario;

    @BeforeEach
    void setUp() {
        vuit = new Person("Виит Вужагиг");
        planet = new Planet("Неизвестная планета");
        penFamily = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        room = new Room("запертая комната", true);
        book = new Book("Теория планет", vuit);
        scenario = new Scenario();
    }

    @Test
    void emptyScenarioHasSizeZero() {
        assertEquals(0, scenario.size());
    }

    @Test
    void addIncreasesSize() {
        scenario.add(new ClaimDiscoveryCommand(vuit, planet));
        assertEquals(1, scenario.size());
    }

    @Test
    void fullStoryChangesStateCorrectly() {
        scenario.add(new ClaimDiscoveryCommand(vuit, planet));
        scenario.add(new VisitPlanetCommand(vuit, planet));
        scenario.add(new WorkAsDriverCommand(vuit, penFamily));
        scenario.add(new ArrestCommand(vuit));
        scenario.add(new LockInRoomCommand(vuit, room));
        scenario.add(new WriteBookCommand(vuit, book, room));
        scenario.add(new SendAwayCommand(vuit, "где люди не платят налогов"));

        scenario.run();

        assertTrue(planet.isDiscovered());
        assertTrue(planet.isVisited());
        assertTrue(penFamily.employs(vuit));
        assertTrue(vuit.isArrested());
        assertTrue(vuit.isLockedUp());
        assertFalse(room.getBooks().isEmpty());
        assertSame(book, room.getBooks().get(0));
    }

    @Test
    void commandsExecuteInOrder() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        scenario.add(new ClaimDiscoveryCommand(vuit, planet));
        scenario.add(new ArrestCommand(vuit));
        scenario.run();

        System.setOut(System.out);
        String output = out.toString();

        int claimPos = output.indexOf("нашёл");
        int arrestPos = output.indexOf("приехали");
        assertTrue(claimPos >= 0 && arrestPos >= 0);
        assertTrue(claimPos < arrestPos);
    }

    @Test
    void addNullCommandThrows() {
        assertThrows(NullPointerException.class, () -> scenario.add(null));
    }

    @Test
    void writeBookRequiresLockInRoomFirst() {
        scenario.add(new ArrestCommand(vuit));
        scenario.add(new LockInRoomCommand(vuit, room));
        scenario.add(new WriteBookCommand(vuit, book, room));
        scenario.run();
        assertFalse(room.getBooks().isEmpty());
    }
}
