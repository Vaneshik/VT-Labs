package lab1.task3;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab1.task3.command.ArrestCommand;
import lab1.task3.command.ClaimDiscoveryCommand;
import lab1.task3.command.LockInRoomCommand;
import lab1.task3.command.VisitPlanetCommand;
import lab1.task3.command.WorkAsDriverCommand;
import lab1.task3.command.WriteBookCommand;
import lab1.task3.model.Book;
import lab1.task3.model.PenFamily;
import lab1.task3.model.Person;
import lab1.task3.model.Planet;
import lab1.task3.model.Room;


class StoryInteractionTest {

    private Person vuit;
    private Planet planet;
    private PenFamily penFamily;
    private Room room;

    @BeforeEach
    void setUp() {
        vuit = new Person("Виит Вужагиг");
        planet = new Planet("Неизвестная планета");
        penFamily = new PenFamily("семья дешёвых зелёных авторучек на пружинках");
        room = new Room("запертая комната", true);
    }

    @Test
    void claimDoesNotAutomaticallyMarkAsVisited() {
        new ClaimDiscoveryCommand(vuit, planet).execute();
        assertTrue(planet.isDiscovered());
        assertFalse(planet.isVisited());
    }

    @Test
    void visitDoesNotAutomaticallyMarkAsDiscovered() {
        new VisitPlanetCommand(vuit, planet).execute();
        assertTrue(planet.isVisited());
        assertFalse(planet.isDiscovered());
    }

    @Test
    void afterClaimAndVisitBothFlagsAreTrue() {
        new ClaimDiscoveryCommand(vuit, planet).execute();
        new VisitPlanetCommand(vuit, planet).execute();
        assertTrue(planet.isDiscovered());
        assertTrue(planet.isVisited());
    }

    // накопление состояний у Person

    @Test
    void personAccumulatesAllStates() {
        new ArrestCommand(vuit).execute();
        new LockInRoomCommand(vuit, room).execute();
        assertTrue(vuit.isArrested());
        assertTrue(vuit.isLockedUp());
    }

    @Test
    void personCanBeReleasedFromLockedUp() {
        vuit.setLockedUp(true);
        vuit.setLockedUp(false);
        assertFalse(vuit.isLockedUp());
    }

    @Test
    void arrestedPersonCanWriteIfLockedUp() {
        new ArrestCommand(vuit).execute();
        new LockInRoomCommand(vuit, room).execute();
        Book book = new Book("Теория планет", vuit);
        assertDoesNotThrow(() -> new WriteBookCommand(vuit, book, room).execute());
        assertEquals(1, room.getBooks().size());
    }

    // несколько книг в комнате

    @Test
    void multipleBooksSavedInRoom() {
        vuit.setLockedUp(true);
        Book b1 = new Book("Глава первая", vuit);
        Book b2 = new Book("Глава вторая", vuit);
        Book b3 = new Book("Глава третья", vuit);
        new WriteBookCommand(vuit, b1, room).execute();
        new WriteBookCommand(vuit, b2, room).execute();
        new WriteBookCommand(vuit, b3, room).execute();
        assertEquals(3, room.getBooks().size());
    }

    @Test
    void booksInRoomPreserveOrder() {
        vuit.setLockedUp(true);
        Book b1 = new Book("Часть I", vuit);
        Book b2 = new Book("Часть II", vuit);
        new WriteBookCommand(vuit, b1, room).execute();
        new WriteBookCommand(vuit, b2, room).execute();
        assertSame(b1, room.getBooks().get(0));
        assertSame(b2, room.getBooks().get(1));
    }

    @Test
    void bookAuthorMatchesWriter() {
        vuit.setLockedUp(true);
        Book book = new Book("Теория планет", vuit);
        new WriteBookCommand(vuit, book, room).execute();
        assertSame(vuit, room.getBooks().get(0).getAuthor());
    }

    // PenFamily несколько сотрудников

    @Test
    void penFamilyCanHireMultiplePeople() {
        Person p2 = new Person("Другой шофёр");
        new WorkAsDriverCommand(vuit, penFamily).execute();
        new WorkAsDriverCommand(p2, penFamily).execute();
        assertEquals(2, penFamily.getEmployees().size());
        assertTrue(penFamily.employs(vuit));
        assertTrue(penFamily.employs(p2));
    }

    @Test
    void nonHiredPersonIsNotEmployee() {
        new WorkAsDriverCommand(vuit, penFamily).execute();
        Person other = new Person("Посторонний");
        assertFalse(penFamily.employs(other));
    }

    @Test
    void employeesListIsUnmodifiable() {
        new WorkAsDriverCommand(vuit, penFamily).execute();
        assertThrows(UnsupportedOperationException.class,
                () -> penFamily.getEmployees().clear());
    }

    // список книг неизменяем снаружи

    @Test
    void booksListIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> room.getBooks().add(new Book("x", vuit)));
    }

    // взаимодействие команд через общие объекты

    @Test
    void writeBookFailsBeforeLockInRoom() {
        new ArrestCommand(vuit).execute();
        Book book = new Book("Ранняя рукопись", vuit);
        assertThrows(IllegalStateException.class,
                () -> new WriteBookCommand(vuit, book, room).execute());
        assertTrue(room.getBooks().isEmpty());
    }

    @Test
    void lockInRoomFailsForUnlockedRoom() {
        Room openRoom = new Room("обычная комната", false);
        assertThrows(IllegalStateException.class,
                () -> new LockInRoomCommand(vuit, openRoom).execute());
        assertFalse(vuit.isLockedUp());
    }

    @Test
    void differentPeopleCanWriteToSameRoom() {
        Person second = new Person("Второй заключённый");
        vuit.setLockedUp(true);
        second.setLockedUp(true);
        Book b1 = new Book("Книга первого", vuit);
        Book b2 = new Book("Книга второго", second);
        new WriteBookCommand(vuit, b1, room).execute();
        new WriteBookCommand(second, b2, room).execute();
        assertEquals(2, room.getBooks().size());
        assertSame(vuit, room.getBooks().get(0).getAuthor());
        assertSame(second, room.getBooks().get(1).getAuthor());
    }

    // взаимодействие команд в цепочке

    @Test
    void scenarioSize_reflectsAddedCommands() {
        Scenario s = new Scenario();
        assertEquals(0, s.size());
        s.add(new ArrestCommand(vuit));
        s.add(new LockInRoomCommand(vuit, room));
        assertEquals(2, s.size());
    }

    @Test
    void scenarioRunModifiesSharedState() {
        Book book = new Book("Рукопись", vuit);
        Scenario s = new Scenario();
        s.add(new ClaimDiscoveryCommand(vuit, planet));
        s.add(new VisitPlanetCommand(vuit, planet));
        s.add(new WorkAsDriverCommand(vuit, penFamily));
        s.add(new ArrestCommand(vuit));
        s.add(new LockInRoomCommand(vuit, room));
        s.add(new WriteBookCommand(vuit, book, room));
        s.run();

        assertTrue(planet.isDiscovered());
        assertTrue(planet.isVisited());
        assertTrue(penFamily.employs(vuit));
        assertTrue(vuit.isArrested());
        assertTrue(vuit.isLockedUp());
        assertEquals(1, room.getBooks().size());
    }

    @Test
    void scenarioRunTwiceDoublesBooks() {
        vuit.setLockedUp(true);
        Book book = new Book("Рукопись", vuit);
        Scenario s = new Scenario();
        s.add(new WriteBookCommand(vuit, book, room));
        s.run();
        s.run();
        assertEquals(2, room.getBooks().size());
    }
}
