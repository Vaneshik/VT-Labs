package enums;

public enum MoveType {
    STRAIGHT("прямо"),
    DEFAULT("дефолтно"),
    CIRCLE("по кругу");

    private final String description;

    MoveType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
