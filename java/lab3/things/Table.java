package things;

public class Table extends MovableThing {
    @Override
    public String describe() {
        return "Стол едет " + moveType + ".";
    }
}
