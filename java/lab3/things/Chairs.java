package things;

import enums.MoveType;

public class Chairs extends Chair {
    private Chair[] chairs = {};

    public Chairs(Chair... chairs) {
        this.chairs = chairs;
    }

    @Override
    public void move(MoveType type) {
        this.moveType = type;
        for (Chair chair : chairs) {
            chair.move(type);
        }
    }

    @Override
    public String describe() {
        return "Стулья едут " + this.moveType + ".";
    }
}
