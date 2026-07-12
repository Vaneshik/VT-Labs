package command;

import manager.CollectionManager;
import manager.ConsoleManager;
import models.forms.OrganizationForm;

/**
 * Команда "remove_greater".
 * Описание команды: удалить из коллекции все элементы, превышающие заданный.
 */
public class RemoveGreaterCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public RemoveGreaterCommand(ConsoleManager console, CollectionManager manager) {
        this.console = console;
        this.manager = manager;
    }

    /**
     * Выполнение команды.
     *
     * @param args аргументы
     */
    @Override
    public int execute(String[] args) {
        if (args.length != 0) {
            console.printError("Команда не принимает аргументы!");
            return 1;
        }
        var size = manager.getCollection().size();
        manager.removeGreater(new OrganizationForm(console).build());
        console.println("Удалено " + String.valueOf(size - manager.getCollection().size()) + " элементов, превышающих заданный");
        return 0;
    }

    @Override
    public String toString() {
        return ": удалить из коллекции все элементы, превышающие заданный";
    }
}
