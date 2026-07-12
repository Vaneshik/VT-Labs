package command;

import manager.CollectionManager;
import manager.ConsoleManager;
import models.forms.OrganizationForm;

/**
 * Команда "add".
 * Описание команды: добавить новый элемент в коллекцию.
 */
public class AddCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public AddCommand(ConsoleManager console, CollectionManager manager) {
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
        manager.add(new OrganizationForm(console).build());
        return 0;
    }

    @Override
    public String toString() {
        return ": добавить новый элемент в коллекцию";
    }
}
