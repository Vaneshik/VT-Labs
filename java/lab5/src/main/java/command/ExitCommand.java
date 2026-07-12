package command;

import manager.ConsoleManager;

/**
 * Команда "exit".
 * Описание команды: завершить программу (без сохранения в файл).
 */
public class ExitCommand implements CommandInterface {
    ConsoleManager console;

    public ExitCommand(ConsoleManager console) {
        this.console = console;
    }

    /**
     * Выполнение команды.
     *
     * @param args аргументы
     */
    @Override
    public int execute(String[] args) {
        if (args.length != 0) {
            console.printError("Команда не принимает аргументы!");
            return 1;
        }

        console.println("Завершаю программу!");
        System.exit(0);
        return 0;
    }

    @Override
    public String toString() {
        return ": завершить программу (без сохранения в файл)";
    }
}
