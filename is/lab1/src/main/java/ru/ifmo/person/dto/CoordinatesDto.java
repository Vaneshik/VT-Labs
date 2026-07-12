package ru.ifmo.person.dto;

public class CoordinatesDto {
    private Double x;
    private Integer y;

    public CoordinatesDto() {
    }

    public CoordinatesDto(Double x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}

