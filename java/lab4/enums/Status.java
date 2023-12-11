package enums;

public enum Status {
    SPILLING("выплескивается"),
    MOVING("едет"),
    ROTATING("вращается"),
    SETTING("устанавливается"),
    RAGING("бушует"),
    RAINING("льет"),
    RUMBLING("гремит"),
    FLASHING("сверкает"),
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
