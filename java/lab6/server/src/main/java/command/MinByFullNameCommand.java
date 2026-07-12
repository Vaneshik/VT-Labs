package command;

import manager.CollectionManager;
import models.Organization;
import network.Response;
import network.User;

/**
 * Команда "min_by_full_name".
 * Описание команды: вывести любой объект из коллекции, значение поля fullName которого является минимальным.
 */
public class MinByFullNameCommand implements CommandInterface {
    CollectionManager manager;

    public MinByFullNameCommand(CollectionManager manager) {
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
        return new Response("Элемент с минимальным значением поля fullName: " + manager.minByFullName(), " ");
    }

    @Override
    public String toString() {
        return ": вывести любой объект из коллекции, значение поля fullName которого является минимальным";
    }
}
