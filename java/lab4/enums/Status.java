package enums;

public enum Status {
    SPILLING("выплескивается"),
    MOVING("едет"),
    ROTATING("вращается"),
    SETTING("устанавливается"),
    DEFAULT("по умолчанию");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
