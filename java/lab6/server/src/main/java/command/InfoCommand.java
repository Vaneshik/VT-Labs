package command;

import manager.CollectionManager;
import models.Organization;
import network.Response;
import network.User;

/**
 * Команда "info".
 * Описание команды: вывести в стандартный поток вывода информацию о коллекции.
 */
public class InfoCommand implements CommandInterface {
    CollectionManager manager;

    public InfoCommand(CollectionManager manager) {
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
            return new Response("Команда не принимает аргументы!", " ");
        }

        StringBuilder response = new StringBuilder();
        response.append("Дата инициализации коллекции: ").append(CollectionManager.initializationTime).append("\n");
        response.append("Тип коллекции: ").append(manager.getCollection().getClass().getName()).append("\n");
        response.append("Размер коллекции: ").append(manager.getCollection().size());
        return new Response(response.toString(), " ");
    }

    @Override
    public String toString() {
        return ": вывести в стандартный поток вывода информацию о коллекции";
    }
}
