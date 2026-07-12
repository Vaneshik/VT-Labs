package command;

import manager.CollectionManager;
import models.Organization;
import network.Response;
import network.User;

/**
 * Команда "filter_contains_name".
 * Описание команды: вывести элементы, значение поля name которых содержит заданную подстроку.
 */
public class FilterContainsNameCommand implements CommandInterface {
    CollectionManager manager;

    public FilterContainsNameCommand(CollectionManager manager) {
        this.manager = manager;
    }

    /**
     * Выполнение команды.
     *
     * @param args               аргументы
     * @param organizationObject
     */
    @Override
    public Response execute(User user, String[] args, Organization organizationObject) {
        if (args.length != 1) {
            return new Response("Команда принимает один аргумент!", " ");
        }
        var filteredOrganizations = manager.filterContainsName(args[0]);
        if (!filteredOrganizations.isEmpty()) {
            StringBuilder response = new StringBuilder();
            response.append("Элементы, значение поля name которых содержит заданную подстроку:").append("\n");
            filteredOrganizations.stream().map(response::append).forEach(s -> response.append("\n"));
            return new Response(response.toString(), " ");
        } else {
            return new Response("Ничего не найдено! :(", " ");
        }
    }

    @Override
    public String toString() {
        return " <name> : вывести элементы, значение поля name которых содержит заданную подстроку";
    }
}
