package command;

import manager.CollectionManager;
import models.Organization;
import models.forms.OrganizationForm;
import network.Response;
import network.User;

/**
 * Команда "remove_greater".
 * Описание команды: удалить из коллекции все элементы, превышающие заданный.
 */
public class RemoveGreaterCommand implements CommandInterface {
    CollectionManager manager;

    public RemoveGreaterCommand(CollectionManager manager) {
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
        manager.removeGreater(organizationObject);
        return new Response("Удалено " + String.valueOf(size - manager.getCollection().size()) + " элементов, превышающих заданный", " ");
    }

    @Override
    public String toString() {
        return ": удалить из коллекции все элементы, превышающие заданный";
    }
}
