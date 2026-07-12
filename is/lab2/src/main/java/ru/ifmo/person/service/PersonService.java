package ru.ifmo.person.service;

import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.enumeration.Color;
import ru.ifmo.person.enumeration.Country;
import ru.ifmo.person.mapper.PersonMapper;
import ru.ifmo.person.model.Person;
import ru.ifmo.person.repository.PersonRepository;
import ru.ifmo.person.websocket.PersonWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    @Inject
    private PersonMapper personMapper;

    public List<PersonDto> getAllPersons() {
        return personRepository.findAll().stream()
                .map(personMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PersonDto> getPersonsPaginated(int page, int pageSize, String sortBy) {
        int offset = page * pageSize;
        return personRepository.findAll(offset, pageSize, sortBy).stream()
                .map(personMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PersonDto> getPersonsFiltered(int page, int pageSize, String name, String nationality, String sortBy) {
        int offset = page * pageSize;
        Country nationalityEnum = (nationality != null && !nationality.isEmpty()) 
            ? Country.valueOf(nationality) 
            : null;
        return personRepository.findFiltered(offset, pageSize, name, nationalityEnum, sortBy).stream()
                .map(personMapper::toDto)
                .collect(Collectors.toList());
    }

    public long getTotalCount() {
        return personRepository.count();
    }

    public long countFiltered(String name, String nationality) {
        Country nationalityEnum = (nationality != null && !nationality.isEmpty()) 
            ? Country.valueOf(nationality) 
            : null;
        return personRepository.countFiltered(name, nationalityEnum);
    }

    public PersonDto getPersonById(Long id) {
        Person person = personRepository.findById(id);
        return personMapper.toDto(person);
    }

    public List<PersonDto> getPersonsByName(String name) {
        return personRepository.findByName(name).stream()
                .map(personMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PersonDto> getPersonsByNationality(Country nationality) {
        return personRepository.findByNationality(nationality).stream()
                .map(personMapper::toDto)
                .collect(Collectors.toList());
    }

    public PersonDto createPerson(PersonDto personDto) {
        Person person = personMapper.toEntity(personDto);
        Integer locationId = person.getLocation() != null ? person.getLocation().getId() : null;
        personRepository.save(person, locationId);
        Person saved = personRepository.findById(person.getId());
        PersonWebSocket.notifyClients("created", saved.getId());
        return personMapper.toDto(saved);
    }

    public PersonDto updatePerson(PersonDto personDto) {
        Person person = personMapper.toEntity(personDto);
        Integer locationId = person.getLocation() != null ? person.getLocation().getId() : null;
        personRepository.update(person, locationId);
        Person updated = personRepository.findById(person.getId());
        PersonWebSocket.notifyClients("updated", updated.getId());
        return personMapper.toDto(updated);
    }

    public void deletePerson(Long id) {
        personRepository.delete(id);
        PersonWebSocket.notifyClients("deleted", id);
    }

    public void deleteByNationality(Country nationality) {
        personRepository.deleteByNationality(nationality);
        PersonWebSocket.notifyClients("bulk_deleted", null);
    }

    public Double getAverageHeight() {
        Double avg = personRepository.getAverageHeight();
        return avg != null ? avg : 0.0;
    }

    public List<Country> getUniqueNationalities() {
        return personRepository.getUniqueNationalities();
    }

    public Double getHairColorPercentage(Color hairColor) {
        return personRepository.getHairColorPercentage(hairColor);
    }

    public Long countByHairColorAndLocation(Color hairColor, Integer locationId) {
        return personRepository.countByHairColorAndLocation(hairColor, locationId);
    }

    public void validatePersonUniqueness(PersonDto personDto) {
        if (personDto.getPassportID() != null && !personDto.getPassportID().isEmpty()) {
            Person existingByPassport = personRepository.findByPassportID(personDto.getPassportID());
            if (existingByPassport != null && 
                (personDto.getId() == null || existingByPassport.getId() != personDto.getId())) {
                throw new IllegalArgumentException("Person with passportID '" + personDto.getPassportID() + "' already exists");
            }
        }

        if (personDto.getName() != null && personDto.getCoordinates() != null) {
            Person existingByNameAndCoords = personRepository.findByNameAndCoordinates(
                personDto.getName(), 
                personDto.getCoordinates().getX(), 
                personDto.getCoordinates().getY()
            );
            if (existingByNameAndCoords != null && 
                (personDto.getId() == null || existingByNameAndCoords.getId() != personDto.getId())) {
                throw new IllegalArgumentException("Person with name '" + personDto.getName() + 
                    "' and coordinates (" + personDto.getCoordinates().getX() + ", " + 
                    personDto.getCoordinates().getY() + ") already exists");
            }
        }
    }
}

