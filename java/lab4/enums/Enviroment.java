package enums;

public enum Enviroment {
    SILENCE("обстановка полной тишины и молчания"),
    NOISE("обстановка шума"),
    PEACEFUL("обстановка мирная"),
    DEFAULT("обстановка по умолчанию");

    private final String description;

    Enviroment(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}