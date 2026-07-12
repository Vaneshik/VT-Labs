package ru.ifmo.person.model;

public class Location {

    private Integer id;
    private int x;
    private float y;
    private int z;
    private String name;

    public Location() {
    }

    public Location(int x, float y, int z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return (name != null ? name + " " : "") + "(" + x + ", " + y + ", " + z + ")";
    }
}
