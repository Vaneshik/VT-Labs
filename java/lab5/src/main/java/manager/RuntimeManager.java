package manager;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Менеджер выполнения программы.
 */
@AllArgsConstructor
public class RuntimeManager {
    private CommandManager commandManager;
    private FileManager fileManager;
    private ConsoleManager console;

    /**
     * Интерактивный режим.
     */
    public void interactiveMode() {
        Scanner scanner = ScannerManager.getScanner();
        fileManager.fillCollection();
        while (true) {
            try {
                console.print(">>> ");
                String[] userCommand = scanner.nextLine().trim().split(" ");
                commandManager.executeCommand(userCommand[0].toLowerCase(), Arrays.copyOfRange(userCommand, 1, userCommand.length));
            } catch (NoSuchElementException e) {
                return;
            } catch (Exception e) {
                console.printError(e.getMessage());
            }
        }
    }
}
