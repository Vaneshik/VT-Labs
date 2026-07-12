package command;

import manager.CollectionManager;
import models.Organization;
import network.Response;
import network.User;

/**
 * Команда "show".
 * Описание команды: вывести в стандартный поток вывода все элементы коллекции в строковом представлении.
 */
public class ShowCommand implements CommandInterface {
    CollectionManager manager;

    public ShowCommand(CollectionManager manager) {
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
        if (args.length != 0) {
            return new Response("Команда не принимает аргументы!", "");
        }
        var collection = manager.getCollection();

        if (collection.isEmpty()) {
            return new Response("Коллекция пуста!", " ");
        } else {
            StringBuilder response = new StringBuilder();
            response.append("Элементы коллекции:").append("\n");
            manager.getCollection().stream().map(response::append).forEach(s -> response.append("\n"));
            return new Response(response.toString(), " ");
        }
    }

    @Override
    public String toString() {
        return ": вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    }
}
