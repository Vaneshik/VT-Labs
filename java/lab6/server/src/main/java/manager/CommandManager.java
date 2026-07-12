package manager;

import command.CommandInterface;
import lombok.Getter;
import lombok.Setter;
import models.Organization;
import network.Response;
import network.User;

import java.util.HashMap;

/**
 * Класс для управления командами.
 */
public class CommandManager {
    @Setter
    @Getter
    HashMap<String, CommandInterface> commands = new HashMap<>();
    /**
     * Добавить команду.
     *
     * @param name    имя команды
     * @param command команда
     */
    public void addCommand(String name, CommandInterface command) {
        commands.put(name, command);
    }

    /**
     * Выполнить команду.
     *
     * @param name имя команды
     * @param args аргументы
     * @return код завершения
     */
    public Response executeCommand(User user, String name, String[] args, Organization organizationObject) {
        CommandInterface command = commands.get(name);
        if (command != null) {
            try {
                return command.execute(user, args, organizationObject);
            } catch (Exception e) {
                System.err.println(e.getClass().getName());
            }
        }
        System.out.println("Команда не найдена! Попробуйте ввести 'help' для получения списка команд.");
        return null;
    }
}
