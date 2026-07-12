package ru.ifmo.person.service;

import ru.ifmo.person.dto.ImportHistoryDto;
import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.enumeration.ImportStatus;
import ru.ifmo.person.mapper.PersonMapper;
import ru.ifmo.person.model.ImportHistory;
import ru.ifmo.person.model.Person;
import ru.ifmo.person.repository.ImportHistoryRepository;
import ru.ifmo.person.repository.PersonRepository;
import ru.ifmo.person.transaction.ImportResult;
import ru.ifmo.person.transaction.ImportTransaction;
import ru.ifmo.person.transaction.Transaction2PCException;
import ru.ifmo.person.transaction.TwoPhaseCommit;
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
    
    @Inject
    private TwoPhaseCommit twoPhaseCommit;
    
    @Inject
    private MinioService minioService;

    public ImportHistoryDto importPersons(List<PersonDto> personDtos, String username, byte[] fileContent, String originalFileName) {
        ImportTransaction transaction = new ImportTransaction(
            personDtos,
            originalFileName,
            fileContent,
            username,
            personMapper,
            personService,
            minioService,
            validator
        );
        
        try {
            ImportResult result = twoPhaseCommit.execute(transaction);
            
            for (Person person : result.getPersons()) {
                PersonWebSocket.notifyClients("created", person.getId());
            }
            
            return toDto(result.getHistory());
            
        } catch (Transaction2PCException e) {
            ImportHistory failedHistory = new ImportHistory();
            failedHistory.setUsername(username);
            failedHistory.setStatus(ImportStatus.FAILED);
            failedHistory.setImportedCount(0);
            failedHistory.setErrorMessage(e.getMessage());
            
            ImportHistory savedHistory = importHistoryRepository.save(failedHistory);
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
        dto.setFileName(history.getFileName());
        dto.setFileUrl(history.getFileUrl());
        dto.setFileSize(history.getFileSize());
        dto.setFileContentType(history.getFileContentType());
        return dto;
    }
}
