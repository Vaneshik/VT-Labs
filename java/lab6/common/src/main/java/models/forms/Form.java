package models.forms;

import util.PromptScan;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Абстрактный класс для формирования объектов классов пользователем.
 *
 * @param <T> Класс формируемого объекта
 */
public abstract class Form<T> {
    private final Scanner scanner = PromptScan.getUserScanner();


    /**
     * Формирует объекта класса
     *
     * @return Объект класса
     */
    public abstract T build();

    /**
     * Запрашивает у пользователя строку
     *
     * @param fieldName    Название поля
     * @param restrictions Ограничения
     * @param validator    Предикат валидации
     * @return Строка
     */
    public String askString(String fieldName, String restrictions, Predicate<String> validator) {
        while (true) {
            System.out.println("Введите " + fieldName + restrictions + ":\n> ");
            String input = scanner.nextLine().trim();
            if (validator.test(input)) {
                return input;
            } else {
                if (input.isEmpty() && validator.test("")) {
                    return null;
                }
                System.err.println("Неверный формат ввода((");
            }
        }
    }

    /**
     * Запрашивает у пользователя Enum
     *
     * @param fieldName      Название поля
     * @param exceptedValues Допустимые значения
     * @param validator      Предикат валидации
     * @return Перечисление
     */
    public Enum askEnum(String fieldName, Enum[] exceptedValues, Predicate<String> validator) {
        while (true) {
            System.out.println("Доступные значения '" + fieldName + "':\n>>> ");
            for (Enum value : exceptedValues) {
                System.out.println(value.toString() + ",");
            }
            System.out.println("Введите " + fieldName + ": ");
            String input = scanner.nextLine().trim();
            if (validator.test(input)) {
                for (Enum value : exceptedValues) {
                    if (value.toString().equals(input.toUpperCase())) {
                        return value;
                    }
                }
                System.err.println("Значение не найдено((");
            } else {
                System.err.println("Неверный формат ввода((");
            }
        }
    }

    /**
     * Запрашивает у пользователя Integer
     *
     * @param fieldName    Название поля
     * @param restrictions Ограничения
     * @param validator    Предикат валидации
     * @return Целое число
     */
    public Integer askInteger(String fieldName, String restrictions, Predicate<Integer> validator) {
        while (true) {
            System.out.println("Введите " + fieldName + restrictions + ":\n>>> ");
            String input = scanner.nextLine().trim();
            try {
                Integer number = Integer.parseInt(input);
                if (validator.test(number)) {
                    return number;
                } else {
                    System.err.println("Ошибка валидации((");
                }
            } catch (NumberFormatException e) {
                if (input.isEmpty() && validator.test(null)) {
                    return null;
                }
                System.err.println("Неверный формат ввода((");
            }
        }
    }

    /**
     * Запрашивает у пользователя Long
     *
     * @param fieldName    Название поля
     * @param restrictions Ограничения
     * @param validator    Предикат валидации
     * @return Длинное число
     */
    public Long askLong(String fieldName, String restrictions, Predicate<Long> validator) {
        while (true) {
            System.out.println("Введите " + fieldName + restrictions + ":\n>>> ");
            String input = scanner.nextLine().trim();
            try {
                Long number = Long.parseLong(input);
                if (validator.test(number)) {
                    return number;
                } else {
                    System.err.println("Ошибка валидации((");
                }
            } catch (NumberFormatException e) {
                if (input.isEmpty() && validator.test(null)) {
                    return null;
                }
                System.err.println("Неверный формат ввода((");
            }
        }
    }

    /**
     * Запрашивает у пользователя Double
     *
     * @param fieldName    Название поля
     * @param restrictions Ограничения
     * @param validator    Предикат валидации
     * @return Дробное число
     */
    public Double askDouble(String fieldName, String restrictions, Predicate<Double> validator) {
        while (true) {
            System.out.println("Введите " + fieldName + restrictions + ":\n>>> ");
            String input = scanner.nextLine().trim();
            try {
                Double number = Double.parseDouble(input);
                if (validator.test(number)) {
                    return number;
                } else {
                    System.err.println("Ошибка валидации((");
                }
            } catch (NumberFormatException e) {
                if (input.isEmpty() && validator.test(null)) {
                    return null;
                }
                System.err.println("Неверный формат ввода((");
            }
        }
    }

    /**
     * Запрашивает у пользователя Float
     *
     * @param fieldName    Название поля
     * @param restrictions Ограничения
     * @param validator    Предикат валидации
     * @return Дробное число
     */
    public Float askFloat(String fieldName, String restrictions, Predicate<Float> validator) {
        while (true) {
            System.out.println("Введите " + fieldName + restrictions + ":\n>>> ");
            String input = scanner.nextLine().trim();
            try {
                Float number = Float.parseFloat(input);
                if (validator.test(number)) {
                    return number;
                } else {
                    System.err.println("Ошибка валидации((");
                }
            } catch (NumberFormatException e) {
                if (input.isEmpty() && validator.test(null))
                    return null;
            }
            System.err.println("Неверный формат ввода((");
        }
    }
}

