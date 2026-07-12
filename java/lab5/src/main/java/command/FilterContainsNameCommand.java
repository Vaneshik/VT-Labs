package command;

import manager.CollectionManager;
import manager.ConsoleManager;

/**
 * Команда "filter_contains_name".
 * Описание команды: вывести элементы, значение поля name которых содержит заданную подстроку.
 */
public class FilterContainsNameCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public FilterContainsNameCommand(ConsoleManager console, CollectionManager manager) {
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
        var filteredOrganizations = manager.filterContainsName(args[0]);
        if (!filteredOrganizations.isEmpty()) {
            console.println("Элементы, значение поля name которых содержит заданную подстроку:");
            filteredOrganizations.stream().map(Object::toString).forEach(console::println);
        } else {
            console.println("Ничего не найдено! :(");
        }

        return 0;
    }

    @Override
    public String toString() {
        return " <name> : вывести элементы, значение поля name которых содержит заданную подстроку";
    }
}
