package creatures;

import enums.MoveType;

public class Moomins extends Moomin {
    private Moomin[] moomins = {};

    public Moomins(Moomin father, Moomin mother, Moomin kid) {
        this.setName(father.getName() + "ы");
        this.moomins = new Moomin[]{father, mother, kid};
    }

    @Override
    public void move(MoveType type) {
        this.moveType = type;
        for (Moomin moomin : moomins) {
            moomin.move(type);
        }
    }

    @Override
    public String describe() {
        return "Семья муми-троллей '" + this.getName() + "' едет " + moveType + ".";
    }
}
