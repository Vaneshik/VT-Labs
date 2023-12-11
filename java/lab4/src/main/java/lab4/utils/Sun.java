package lab4.utils;


import lab4.enums.Status;
import lab4.enums.Enviroment;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

public class Sun {
    @Getter @Setter private Status status = Status.DEFAULT;
    @Getter @Setter private Enviroment enviroment = Enviroment.DEFAULT;

//    public void setStatus(Status status) {
//        this.status = status;
//    }
//
//    public Status getStatus() {
//        return status;
//    }

    public void goDown() {
        setStatus(Status.SETTING);
    }

    public void setEnviroment(Enviroment enviroment) {
        this.enviroment = enviroment;
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
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, enviroment);
    }
}
