package ru.ifmo.person.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public class CoordinatesDto {
    @NotNull(message = "X  не может быть null")
    @Max(value = 454, message = "X не больше 454")
    private Double x;
    
    @NotNull(message = "Y не может быть null")
    @Max(value = 698, message = "Y не больше 698")
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

