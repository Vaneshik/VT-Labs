package command;

/**
 * Интерфейс команд.
 */
public interface CommandInterface {
    /**
     * Выполняет команду
     *
     * @param args аргументы
     */
    int execute(String[] args);
}
