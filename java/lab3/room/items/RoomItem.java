package room.items;

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

    public String toString() {
        return "Предмет комнаты";
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return ((RoomItem) o).status == this.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}
