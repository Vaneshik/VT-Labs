package manager;

/**
 * Класс для работы с консолью.
 */
public class ConsoleManager {
    /**
     * Вывести текст в консоль на отдельной строке.
     *
     * @param text текст
     */
    public void println(String text) {
        System.out.println(text);
    }

    /**
     * Вывести текст в консоль.
     *
     * @param text текст
     */
    public void print(String text) {
        System.out.print(text);
    }

    /**
     * Вывести текст об ошибке в консоль.
     *
     * @param text текст
     */
    public void printError(String text) {
        System.err.println(text);
    }
}
