package example.entity;

import java.io.Serializable;

public class ResultEntity implements Serializable {
    private Long id;
    private double x;
    private double y;
    private double r;
    private boolean hit;

    public ResultEntity(double x, double y, double r, boolean hit) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
    }

    public ResultEntity(Long id, double x, double y, double r, boolean hit) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getR() {
        return r;
    }
    public void setR(double r) {
        this.r = r;
    }

    public boolean isHit() {
        return hit;
    }
    public void setHit(boolean hit) {
        this.hit = hit;
    }
}