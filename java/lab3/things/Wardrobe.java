package things;

public class Wardrobe extends MovableThing {
    @Override
    public String describe() {
        return "Плятяной шкаф едет " + moveType + ".";
    }
}
