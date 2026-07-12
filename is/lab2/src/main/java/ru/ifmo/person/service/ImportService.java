package ru.ifmo.person.service;

import ru.ifmo.person.dto.ImportHistoryDto;
import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.enumeration.ImportStatus;
import ru.ifmo.person.mapper.PersonMapper;
import ru.ifmo.person.model.ImportHistory;
import ru.ifmo.person.model.Person;
import ru.ifmo.person.repository.ImportHistoryRepository;
import ru.ifmo.person.repository.PersonRepository;
import ru.ifmo.person.websocket.PersonWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ImportService {

    @Inject
    private PersonRepository personRepository;

    @Inject
    private ImportHistoryRepository importHistoryRepository;

    @Inject
    private PersonMapper personMapper;

    @Inject
    private PersonService personService;

    @Inject
    private Validator validator;

    @Inject
    private SessionFactory sessionFactory;

    public ImportHistoryDto importPersons(List<PersonDto> personDtos, String username) {
        Transaction tx = null;
        ImportHistory history = new ImportHistory();
        history.setUsername(username);
        
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            
            List<Person> personsToSave = new ArrayList<>();
            
            for (int i = 0; i < personDtos.size(); i++) {
                PersonDto personDto = personDtos.get(i);
                
                var violations = validator.validate(personDto);
                if (!violations.isEmpty()) {
                    String msg = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                    throw new IllegalArgumentException("Record " + (i + 1) + ": " + msg);
                }
                
                personService.validatePersonUniqueness(personDto);
                
                for (int j = 0; j < i; j++) {
                    PersonDto existingDto = personDtos.get(j);
                    
                    if (personDto.getPassportID() != null && 
                        personDto.getPassportID().equals(existingDto.getPassportID())) {
                        throw new IllegalArgumentException(
                            "Duplicate passportID '" + personDto.getPassportID() + 
                            "' found within import batch at positions " + (j+1) + " and " + (i+1));
                    }
                    
                    if (personDto.getName() != null && personDto.getCoordinates() != null &&
                        existingDto.getName() != null && existingDto.getCoordinates() != null &&
                        personDto.getName().equals(existingDto.getName()) &&
                        personDto.getCoordinates().getX().equals(existingDto.getCoordinates().getX()) &&
                        personDto.getCoordinates().getY().equals(existingDto.getCoordinates().getY())) {
                        throw new IllegalArgumentException(
                            "Duplicate name+coordinates ('" + personDto.getName() + "' at " +
                            personDto.getCoordinates().getX() + "," + personDto.getCoordinates().getY() +
                            ") found within import batch at positions " + (j+1) + " and " + (i+1));
                    }
                }
                
                Person person = personMapper.toEntity(personDto);
                personsToSave.add(person);
            }
            
            personRepository.saveAllInTransaction(personsToSave, session);
            
            history.setStatus(ImportStatus.SUCCESS);
            history.setImportedCount(personsToSave.size());
            history.setErrorMessage(null);
            session.persist(history);
            session.flush();
            session.refresh(history);
            
            tx.commit();
            
            for (Person person : personsToSave) {
                PersonWebSocket.notifyClients("created", person.getId());
            }
            
            return toDto(history);
            
        } catch (Exception e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                }
            }
            
            history.setStatus(ImportStatus.FAILED);
            history.setImportedCount(0);
            history.setErrorMessage(e.getMessage());
            ImportHistory savedHistory = importHistoryRepository.save(history);
            return toDto(savedHistory);
        }
    }

    public List<ImportHistoryDto> getAllHistory() {
        return importHistoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ImportHistoryDto> getHistoryByUsername(String username) {
        return importHistoryRepository.findByUsername(username).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ImportHistoryDto getHistoryById(Long id) {
        ImportHistory history = importHistoryRepository.findById(id);
        return history != null ? toDto(history) : null;
    }

    private ImportHistoryDto toDto(ImportHistory history) {
        ImportHistoryDto dto = new ImportHistoryDto();
        dto.setId(history.getId());
        dto.setStatus(history.getStatus());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setUsername(history.getUsername());
        dto.setImportedCount(history.getImportedCount());
        dto.setErrorMessage(history.getErrorMessage());
        return dto;
    }
}
