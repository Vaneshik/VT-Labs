package ru.ifmo.person.mapper;

import ru.ifmo.person.dto.LocationDto;
import ru.ifmo.person.model.Location;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LocationMapper {

    public LocationDto toDto(Location location) {
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

    public Location toEntity(LocationDto dto) {
        if (dto == null) {
            return null;
        }

        Location location = new Location();
        location.setId(dto.getId());
        location.setX(dto.getX());
        location.setY(dto.getY());
        location.setZ(dto.getZ());
        location.setName(dto.getName());
        return location;
    }
}

