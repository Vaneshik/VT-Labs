package creatures;

import things.MovableThing;

public abstract class Creature extends MovableThing {
    private boolean isAlive = true;
    private String name;

    public Creature() {
        this.name = "";
    }

    public Creature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean checkHealth() {
        return this.isAlive;
    }

    void kill() {
        this.isAlive = false;
    }

    @Override
    public String describe() {
        return this.name;
    }
}
