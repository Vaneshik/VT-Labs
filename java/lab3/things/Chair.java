package things;

public class Chair extends MovableThing {
    @Override
    public String describe() {
        return "Стул едет " + this.moveType + ".";
    }
}
