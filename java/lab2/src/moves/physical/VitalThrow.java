package moves.physical;

import ru.ifmo.se.pokemon.*;

public class VitalThrow extends PhysicalMove {
    public VitalThrow(){
        super(Type.FIGHTING, 70, Double.POSITIVE_INFINITY);
    }

    @Override
    protected String describe() {
        return "использует Vital Throw";
    }
}
