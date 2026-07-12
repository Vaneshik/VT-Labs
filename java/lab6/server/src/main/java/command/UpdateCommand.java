package command;

import manager.CollectionManager;
import models.Organization;
import models.forms.OrganizationForm;
import network.Response;
import network.User;

/**
 * Команда "update".
 * Описание команды: обновить значение элемента коллекции, id которого равен заданному.
 */
public class UpdateCommand implements CommandInterface {
    CollectionManager manager;

    public UpdateCommand(CollectionManager manager) {
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
            return new Response("Команда принимает один аргумент!", "");
        }
        if (!args[0].matches("\\d+")) {
            return new Response("id должен быть числом!", " ");
        }
        long id = Long.parseLong(args[0]);
        if (manager.getById(id) == null) {
            return new Response("Элемента с таким id нет в коллекции!", " ");
        }
        manager.update(id, organizationObject);
        return new Response("Обновлен элемент с id " + args[0], " ");
    }

    @Override
    public String toString() {
        return " <id> : обновить значение элемента коллекции, id которого равен заданному";
    }
}
