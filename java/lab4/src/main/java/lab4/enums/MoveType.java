package lab4.enums;

public enum MoveType {
    STRAIGHT("прямо"),
    BACK("назад"),
    LEFT("налево"),
    RIGHT("направо"),
    UP("вверх"),
    DOWN("вниз"),
    CIRCLE("по кругу"),
    DEFAULT("по умолчанию");

    private final String description;

    MoveType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
