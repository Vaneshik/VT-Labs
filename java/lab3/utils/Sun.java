package utils;

import enums.Enviroment;
import enums.Status;

public class Sun {
    private Status status = Status.DEFAULT;
    private Enviroment enviroment = Enviroment.DEFAULT;

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void goDown() {
        setStatus(Status.SETTING);
    }

    public void setEnviroment(Enviroment enviroment) {
        this.enviroment = enviroment;
    }

    public String describe() {
        return "Солнце садится, " + enviroment + ".";
    }
}
