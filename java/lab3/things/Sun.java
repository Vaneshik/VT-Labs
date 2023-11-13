package things;

import enums.Enviroment;
import enums.Status;
import interfaces.Downable;

public class Sun extends Thing implements Downable {
    private Enviroment enviroment = Enviroment.DEFAULT;

    @Override
    public void goDown() {
        setStatus(Status.SETTING);
    }

    public void setEnviroment(Enviroment enviroment) {
        this.enviroment = enviroment;
    }

    @Override
    public String describe() {
        return "Солнце садится, " + enviroment + ".";
    }
}
