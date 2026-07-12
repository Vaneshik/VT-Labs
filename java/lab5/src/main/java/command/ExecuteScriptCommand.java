package command;

import manager.*;

import java.io.File;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Команда "execute_script".
 * Описание команды: считать и исполнить скрипт из указанного файла.
 */
public class ExecuteScriptCommand implements CommandInterface {
    ConsoleManager console;
    CommandManager commandManager;

    public ExecuteScriptCommand(ConsoleManager console, CommandManager commandManager) {
        this.console = console;
        this.commandManager = commandManager;
    }

    /**
     * Выполнение команды.
     *
     * @param args аргументы
     */
    @Override
    public int execute(String[] args) {
        if (args.length != 1) {
            console.printError("Команда принимает один аргумент!");
            return 1;
        }

        String filename = args[0];

        File file = new File(filename);

        if (!FileManager.canRead(file, console)) {
            return 2;
        }

        try {
            ScriptManager.addFile(filename);
            Scanner manager;

            while ((manager = ScriptManager.getLastScanner()) != null) {
                ScannerManager.setScanner(manager);
                String line = manager.nextLine();
                String[] command = line.trim().split(" ");

                if (command[0].equalsIgnoreCase("execute_script") && ScriptManager.isRecursive(command[1])) {
                    console.printError("Найдена рекурсия! Повторно вызывается файл " +
                            new File(command[1]).getAbsolutePath());
                    continue;
                }

                console.println("Выполнение команды " + command[0] + ":");
                if (commandManager.getCommands().get(command[0]) != null) {
                    var statusCode =  commandManager.executeCommand(command[0], Arrays.copyOfRange(command, 1, command.length));
                    if (statusCode != 0) {
                        return statusCode;
                    }
                } else {
                    console.printError("Такой команды нет!((");
                    return 3;
                }
            }
            ScriptManager.removeFile();
            ScannerManager.setScanner(new Scanner(System.in));
        } catch (NoSuchElementException e) {
            return 4;
        } catch (Exception e) {
            console.printError(e.getMessage());
            return 5;
        }

        return 0;

    }


    @Override
    public String toString() {
        return " <filename> : считать и исполнить скрипт из указанного файла";
    }
}
