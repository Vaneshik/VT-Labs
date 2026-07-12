package command;

import manager.CollectionManager;
import models.Organization;
import network.Response;
import network.User;

/**
 * Команда "add_if_min".
 * Описание команды: добавить новый элемент, если его значение меньше, чем у наименьшего элемента коллекции.
 */
public class AddIfMinCommand implements CommandInterface {
    CollectionManager manager;

    public AddIfMinCommand(CollectionManager manager) {
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
        var size = manager.getCollection().size();
        manager.addIfMin(organizationObject);
        if (size == manager.getCollection().size())
            return new Response("Элемент не добавлен (", " ");
        else {
            return new Response("Элемент добавлен!", " ");
        }
    }

    @Override
    public String toString() {
        return ": добавить новый элемент, если его значение меньше, чем у наименьшего элемента коллекции";
    }
}
