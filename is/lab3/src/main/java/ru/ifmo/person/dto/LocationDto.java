package ru.ifmo.person.dto;

import jakarta.validation.constraints.Size;

public class LocationDto {
    private Integer id;
    private Integer x;
    private Float y;
    private Integer z;
    
    @Size(max = 953, message = "Длина должна быть не больше 953 символов")
    private String name;

    public LocationDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

