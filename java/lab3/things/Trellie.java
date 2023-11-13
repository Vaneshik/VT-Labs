package things;

public class Trellie extends MovableThing {
    @Override
    public String describe() {
        return "Трельяж едет " + moveType + ".";
    }
}
