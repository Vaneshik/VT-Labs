package ru.ifmo.person.dto;

import ru.ifmo.person.enumeration.Color;
import ru.ifmo.person.enumeration.Country;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.Date;

public class PersonDto {
    private Long id;
    
    @NotNull(message = "имя не может быть null")
    @NotBlank(message = "имя не может быть пустым")
    private String name;
    
    @NotNull(message = "координаты не могут быть null")
    @Valid
    private CoordinatesDto coordinates;
    
    private Date creationDate;
    
    @NotNull(message = "цвет глаз не может быть null")
    private Color eyeColor;
    
    private Color hairColor;
    
    @Valid
    private LocationDto location;
    
    @Min(value = 1, message = "Height > 0")
    private Long height;
    
    @Min(value = 1, message = "Weight > 0")
    private Integer weight;
    
    private String passportID;
    
    private Country nationality;

    public PersonDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(Color eyeColor) {
        this.eyeColor = eyeColor;
    }

    public Color getHairColor() {
        return hairColor;
    }

    public void setHairColor(Color hairColor) {
        this.hairColor = hairColor;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country nationality) {
        this.nationality = nationality;
    }
}

