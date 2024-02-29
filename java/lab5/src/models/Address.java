package models;

import models.Location;

public class Address {
    private String zipCode; //Поле не может быть null
    private Location town; //Поле может быть null

    public Address(String zipCode, Location town) {
        this.zipCode = zipCode;
        this.town = town;
    }
}
