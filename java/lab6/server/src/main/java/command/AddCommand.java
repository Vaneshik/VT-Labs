package command;
import models.Organization;
import manager.CollectionManager;
import network.Response;
import network.User;

/**
 * Команда "add".
 * Описание команды: добавить новый элемент в коллекцию.
 */
public class AddCommand implements CommandInterface {
    CollectionManager manager;

    public AddCommand(CollectionManager manager) {
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

        manager.add(organizationObject, true);
        return new Response("Элемент добавлен!", " ");
    }

    @Override
    public String toString() {
        return ": добавить новый элемент в коллекцию";
    }
}
