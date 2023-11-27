package room;

import enums.Status;

import java.util.Objects;

public abstract class RoomItem {
    private Status status = Status.DEFAULT;

    public Status getStatus() {
        return status;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    public abstract String describe();

    @Override
    public String toString() {
        return describe();
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
        return Objects.hash(status);
    }
}
