package command;

import manager.CollectionManager;
import manager.ConsoleManager;
import models.forms.OrganizationForm;

/**
 * Команда "remove_lower".
 * Описание команды: удалить из коллекции все элементы, меньшие, чем заданный.
 */
public class RemoveLowerCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public RemoveLowerCommand(ConsoleManager console, CollectionManager manager) {
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
        manager.removeLower(new OrganizationForm(console).build());
        console.println("Удалено " + String.valueOf(size - manager.getCollection().size()) + " элементов, меньшие, чем заданный");
        return 0;
    }

    @Override
    public String toString() {
        return ": удалить из коллекции все элементы, меньшие, чем заданный";
    }
}
