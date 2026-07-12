package ru.ifmo.person.service;

import ru.ifmo.person.dto.LocationDto;
import ru.ifmo.person.mapper.LocationMapper;
import ru.ifmo.person.model.Location;
import ru.ifmo.person.repository.LocationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LocationService {

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private LocationMapper locationMapper;

    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<LocationDto> getDistinctLocations() {
        return locationRepository.findDistinctLocations().stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }

    public LocationDto getLocationById(Integer id) {
        Location location = locationRepository.findById(id);
        return locationMapper.toDto(location);
    }

    public LocationDto createLocation(LocationDto locationDto) {
        Location location = locationMapper.toEntity(locationDto);
        locationRepository.save(location);
        return locationMapper.toDto(location);
    }

    public LocationDto updateLocation(LocationDto locationDto) {
        Location location = locationMapper.toEntity(locationDto);
        Location updated = locationRepository.update(location);
        return locationMapper.toDto(updated);
    }

    public void deleteLocation(Integer id) {
        locationRepository.delete(id);
    }
}

