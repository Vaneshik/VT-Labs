package utils;

import enums.Enviroment;
import enums.Status;

import java.util.Objects;

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

    public Enviroment getEnviroment() {
        return enviroment;
    }

    public String describe() {
        return this + " садится, " + enviroment + ".";
    }

    @Override
    public String toString() {
        return "Солнце";
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return status == ((Sun) o).status &&
                enviroment == ((Sun) o).enviroment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, enviroment);
    }
}
