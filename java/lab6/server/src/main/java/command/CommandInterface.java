package command;

import models.Organization;
import network.Response;
import network.User;

/**
 * Интерфейс команд.
 */
public interface CommandInterface {
    /**
     * Выполняет команду
     *
     * @param args               аргументы
     * @param organizationObject
     */
    Response execute(User user, String[] args, Organization organizationObject);
}
