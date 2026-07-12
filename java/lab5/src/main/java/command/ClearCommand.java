package command;

import manager.CollectionManager;
import manager.ConsoleManager;

/**
 * Команда "clear".
 * Описание команды: очистить коллекцию.
 */
public class ClearCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public ClearCommand(ConsoleManager console, CollectionManager manager) {
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

        console.println("Коллекция очищена");
        manager.clear();

        return 0;
    }

    @Override
    public String toString() {
        return ": очистить коллекцию";
    }
}
