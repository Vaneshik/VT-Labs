package moves.physical;

import ru.ifmo.se.pokemon.*;

/**
 * Double Slap hits 2-5 times per turn used.
 * The probability of each interval is shown in the table, with the total power after each hit.
 * Each strike of Double Slap is treated like a separate attack.
 */

public class DoubleSlap extends PhysicalMove {
    public DoubleSlap() {
        super(Type.NORMAL, 15, 85);
    }

    @Override
    protected double calcBaseDamage(Pokemon var1, Pokemon var2) {
        double start_power = this.power;
        for (int i = 0; i < 4; i++) {
            if (i < 2) {
                if (3.0 / 8.0 > Math.random()) start_power += this.power;
            } else {
                if (1.0 / 8.0 > Math.random()) start_power += this.power;
            }
        }
        return (0.4 * (double) var1.getLevel() + 2.0) * start_power / 150.0;
    }

    @Override
    protected String describe() {
        return "использует Double Slap";
    }
}
