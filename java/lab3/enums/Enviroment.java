package enums;

public enum Enviroment {
    SILENCE("обстановка полной тишины и молчания"),
    NOISE("обстановка шума"),
    DEFAULT("обстановка по умолчаниб");

    private final String description;

    Enviroment(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}