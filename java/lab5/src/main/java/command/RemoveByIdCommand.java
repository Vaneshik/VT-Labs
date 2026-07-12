package command;

import manager.CollectionManager;
import manager.ConsoleManager;

/**
 * Команда "remove_by_id".
 * Описание команды: удалить элемент из коллекции по его id.
 */
public class RemoveByIdCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public RemoveByIdCommand(ConsoleManager console, CollectionManager manager) {
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
        if (args.length != 1) {
            console.printError("Команда принимает один аргумент!");
            return 1;
        }

        try {
            long id = Long.parseLong(args[0]);
            if (manager.getById(id) == null) {
                console.printError("Элемент с id " + id + " не найден");
                return 2;
            }

            manager.removeById(id);
            console.println("Элемент с id " + id + " удален");
            return 0;
        } catch (NumberFormatException e) {
            console.printError("Неверный формат аргумента!");
            return 3;
        }
    }

    @Override
    public String toString() {
        return " <id> : удалить элемент из коллекции по его id";
    }
}
