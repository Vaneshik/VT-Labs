package enums;

public enum Enviroment {
    SILENCE("обстановка полной тишины и молчания"),
    NOISE("обстановка разгрома и криков о помощи"),
    DEFAULT("обстановка дефолта");

    private final String description;

    Enviroment(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
