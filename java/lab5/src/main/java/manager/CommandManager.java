package manager;

import command.CommandInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

/**
 * Класс для управления командами.
 */
public class CommandManager {
    @Setter
    @Getter
    HashMap<String, CommandInterface> commands = new HashMap<>();
    ConsoleManager console;

    public CommandManager(ConsoleManager console) {
        this.console = console;
    }

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
    public int executeCommand(String name, String[] args) {
        CommandInterface command = commands.get(name);
        if (command != null) {
            try {
                return command.execute(args);
            } catch (Exception e) {
                console.printError(e.getClass().getName());
                return -1;
            }
        }
        console.println("Команда не найдена! Попробуйте ввести 'help' для получения списка команд.");
        return -1;
    }
}
