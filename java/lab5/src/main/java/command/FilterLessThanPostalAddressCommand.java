package command;

import manager.CollectionManager;
import manager.ConsoleManager;
import models.Organization;
import models.forms.AddressForm;

/**
 * Команда "filter_less_than_postal_address".
 * Описание команды: вывести элементы, значение поля postalAddress которых меньше заданного.
 */
public class FilterLessThanPostalAddressCommand implements CommandInterface {
    ConsoleManager console;
    CollectionManager manager;

    public FilterLessThanPostalAddressCommand(ConsoleManager console, CollectionManager manager) {
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

        var response = manager.filterLessThanPostalAddress(new AddressForm(console).build());
        if (response.isEmpty()) {
            console.println("Элементов не найдено");
        } else {
            console.println("Элементы, значение поля postalAddress которых меньше заданного:");
            response.stream().map(Organization::toString).forEach(console::println);
        }
        return 0;
    }

    @Override
    public String toString() {
        return ": вывести элементы, значение поля postalAddress которых меньше заданного";
    }
}
