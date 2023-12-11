package lab4.enums;

public enum LiquidType {
    TEA("Чай"),
    COFFEE("Кофе"),
    WATER("Вода"),
    MILK("Молоко"),
    JUICE("Сок"),
    BEER("Пиво"),
    WINE("Вино"),
    DEFAULT("по умолчанию");

    private final String description;

    LiquidType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
