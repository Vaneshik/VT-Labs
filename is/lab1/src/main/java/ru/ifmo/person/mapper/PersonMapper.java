package ru.ifmo.person.mapper;

import ru.ifmo.person.dto.CoordinatesDto;
import ru.ifmo.person.dto.LocationDto;
import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.model.Coordinates;
import ru.ifmo.person.model.Location;
import ru.ifmo.person.model.Person;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonMapper {

    public PersonDto toDto(Person person) {
        if (person == null) {
            return null;
        }

        PersonDto dto = new PersonDto();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setCreationDate(person.getCreationDate());
        dto.setEyeColor(person.getEyeColor());
        dto.setHairColor(person.getHairColor());
        dto.setHeight(person.getHeight());
        dto.setWeight(person.getWeight());
        dto.setPassportID(person.getPassportID());
        dto.setNationality(person.getNationality());

        if (person.getCoordinates() != null) {
            dto.setCoordinates(toCoordinatesDto(person.getCoordinates()));
        }

        if (person.getLocation() != null) {
            dto.setLocation(toLocationDto(person.getLocation()));
        }

        return dto;
    }

    public Person toEntity(PersonDto dto) {
        if (dto == null) {
            return null;
        }

        Person person = new Person();
        if (dto.getId() != null) {
            person.setId(dto.getId());
            person.setCreationDate(dto.getCreationDate());
        } else {
            person.setCreationDate(new java.util.Date());
        }
        person.setName(dto.getName());
        person.setEyeColor(dto.getEyeColor());
        person.setHairColor(dto.getHairColor());
        person.setHeight(dto.getHeight());
        person.setWeight(dto.getWeight());
        person.setPassportID(dto.getPassportID());
        person.setNationality(dto.getNationality());

        if (dto.getCoordinates() != null) {
            person.setCoordinates(toCoordinatesEntity(dto.getCoordinates()));
        }

        if (dto.getLocation() != null) {
            person.setLocation(toLocationEntity(dto.getLocation()));
        }

        return person;
    }

    private CoordinatesDto toCoordinatesDto(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }
        return new CoordinatesDto(coordinates.getX(), coordinates.getY());
    }

    private Coordinates toCoordinatesEntity(CoordinatesDto dto) {
        if (dto == null) {
            return null;
        }
        Coordinates coordinates = new Coordinates();
        coordinates.setX(dto.getX());
        coordinates.setY(dto.getY());
        return coordinates;
    }

    private LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setX(location.getX());
        dto.setY(location.getY());
        dto.setZ(location.getZ());
        dto.setName(location.getName());
        return dto;
    }

    private Location toLocationEntity(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        Location location = new Location();
        location.setId(dto.getId());
        if (dto.getX() != null && dto.getY() != null && dto.getZ() != null) {
            location.setX(dto.getX());
            location.setY(dto.getY());
            location.setZ(dto.getZ());
            location.setName(dto.getName());
        }
        return location;
    }
}

