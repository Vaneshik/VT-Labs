package command;

import manager.ConsoleManager;
import manager.FileManager;

/**
 * Команда "save".
 * Описание команды: сохранить коллекцию в файл.
 */
public class SaveCommand implements CommandInterface {
    FileManager fileManager;
    ConsoleManager console;

    public SaveCommand(ConsoleManager console, FileManager fileManager) {
        this.fileManager = fileManager;
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

        try {
            fileManager.saveCollection();
        } catch (Exception e) {
            console.printError(e.getMessage());
            return 2;
        }

        console.println("Коллекция сохранена в файл");
        return 0;
    }

    @Override
    public String toString() {
        return ": сохранить коллекцию в файл";
    }
}
