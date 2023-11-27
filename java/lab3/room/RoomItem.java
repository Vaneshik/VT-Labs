package room;

import enums.Status;

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
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        return hashCode() == o.hashCode();
    }
}
