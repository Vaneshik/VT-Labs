package ru.ifmo.person.transaction;

import io.minio.MinioClient;
import jakarta.validation.Validator;
import org.hibernate.Session;
import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.enumeration.ImportStatus;
import ru.ifmo.person.mapper.PersonMapper;
import ru.ifmo.person.model.ImportHistory;
import ru.ifmo.person.model.Person;
import ru.ifmo.person.repository.PersonRepository;
import ru.ifmo.person.service.MinioService;
import ru.ifmo.person.service.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ImportTransaction implements Transaction2PC<ImportResult> {

    private static final Logger LOGGER = Logger.getLogger(ImportTransaction.class.getName());

    private final String transactionId;
    private final List<PersonDto> personDtos;
    private final String originalFileName;
    private final byte[] fileContent;
    private final String username;
    
    private final PersonMapper personMapper;
    private final PersonService personService;
    private final MinioService minioService;
    private final Validator validator;
    
    private ImportHistory history;
    private List<Person> personsToSave;
    private String minioFileName;
    private boolean minioFileUploaded = false;

    public ImportTransaction(
            List<PersonDto> personDtos,
            String originalFileName,
            byte[] fileContent,
            String username,
            PersonMapper personMapper,
            PersonService personService,
            MinioService minioService,
            Validator validator) {
        
        this.transactionId = UUID.randomUUID().toString();
        this.personDtos = personDtos;
        this.originalFileName = originalFileName;
        this.fileContent = fileContent;
        this.username = username;
        this.personMapper = personMapper;
        this.personService = personService;
        this.minioService = minioService;
        this.validator = validator;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public PrepareResult prepareDatabase(Session session) throws Exception {
        try {
            LOGGER.info(String.format("[TX:%s] Preparing database: validating %d persons", transactionId, personDtos.size()));
            
            personsToSave = new ArrayList<>();
            
            for (int i = 0; i < personDtos.size(); i++) {
                PersonDto personDto = personDtos.get(i);
                
                var violations = validator.validate(personDto);
                if (!violations.isEmpty()) {
                    String msg = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                    return PrepareResult.failure("Record " + (i + 1) + ": " + msg);
                }
                
                personService.validatePersonUniqueness(personDto);
                
                for (int j = 0; j < i; j++) {
                    PersonDto existingDto = personDtos.get(j);
                    
                    if (personDto.getPassportID() != null && 
                        personDto.getPassportID().equals(existingDto.getPassportID())) {
                        return PrepareResult.failure(
                            "Duplicate passportID '" + personDto.getPassportID() + 
                            "' found within import batch at positions " + (j+1) + " and " + (i+1));
                    }
                    
                    if (personDto.getName() != null && personDto.getCoordinates() != null &&
                        existingDto.getName() != null && existingDto.getCoordinates() != null &&
                        personDto.getName().equals(existingDto.getName()) &&
                        personDto.getCoordinates().getX().equals(existingDto.getCoordinates().getX()) &&
                        personDto.getCoordinates().getY().equals(existingDto.getCoordinates().getY())) {
                        return PrepareResult.failure(
                            "Duplicate name+coordinates ('" + personDto.getName() + "' at " +
                            personDto.getCoordinates().getX() + "," + personDto.getCoordinates().getY() +
                            ") found within import batch at positions " + (j+1) + " and " + (i+1));
                    }
                }
                
                Person person = personMapper.toEntity(personDto);
                personsToSave.add(person);
            }
            
            history = new ImportHistory();
            history.setUsername(username);
            history.setStatus(ImportStatus.SUCCESS);
            history.setImportedCount(0);
            
            LOGGER.info(String.format("[TX:%s] Database preparation successful", transactionId));
            return PrepareResult.success();
            
        } catch (Exception e) {
            LOGGER.severe(String.format("[TX:%s] Database preparation failed: %s", transactionId, e.getMessage()));
            return PrepareResult.failure("Database preparation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PrepareResult prepareMinIO(MinioClient client) throws Exception {
        try {
            LOGGER.info(String.format("[TX:%s] Preparing MinIO storage", transactionId));
            
            if (!minioService.isMinioAvailable()) {
                return PrepareResult.failure("MinIO is not available");
            }
            
            minioService.ensureBucketExists();
            
            minioFileName = String.format("%s_%s_%s", 
                username, 
                System.currentTimeMillis(), 
                originalFileName);
            
            LOGGER.info(String.format("[TX:%s] MinIO preparation successful, file will be: %s", transactionId, minioFileName));
            return PrepareResult.success();
            
        } catch (Exception e) {
            LOGGER.severe(String.format("[TX:%s] MinIO preparation failed: %s", transactionId, e.getMessage()));
            return PrepareResult.failure("MinIO preparation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ImportResult commitDatabase(Session session) throws Exception {
        try {
            LOGGER.info(String.format("[TX:%s] Committing to database: inserting %d persons", transactionId, personsToSave.size()));
            
            for (Person person : personsToSave) {
                session.persist(person);
            }
            session.flush();
            
            history.setStatus(ImportStatus.SUCCESS);
            history.setImportedCount(personsToSave.size());
            history.setFileName(minioFileName);
            history.setFileSize((long) fileContent.length);
            history.setFileContentType("application/json");
            
            session.persist(history);
            session.flush();
            session.refresh(history);
            
            LOGGER.info(String.format("[TX:%s] Database commit successful, imported %d persons", transactionId, personsToSave.size()));
            return new ImportResult(history, personsToSave);
            
        } catch (Exception e) {
            LOGGER.severe(String.format("[TX:%s] Database commit failed: %s", transactionId, e.getMessage()));
            throw e;
        }
    }

    @Override
    public void commitMinIO(MinioClient client) throws Exception {
        try {
            LOGGER.info(String.format("[TX:%s] Committing to MinIO: uploading file %s", transactionId, minioFileName));
            
            minioService.uploadFile(minioFileName, fileContent, "application/json");
            minioFileUploaded = true;
            
            String presignedUrl = minioService.generatePresignedUrl(minioFileName);
            
            history.setFileUrl(presignedUrl);
            
            LOGGER.info(String.format("[TX:%s] MinIO commit successful, file uploaded: %s", transactionId, minioFileName));
            
        } catch (Exception e) {
            LOGGER.severe(String.format("[TX:%s] MinIO commit failed: %s", transactionId, e.getMessage()));
            throw e;
        }
    }

    @Override
    public void rollbackDatabase(Session session) {
        try {
            LOGGER.info(String.format("[TX:%s] Rolling back database changes", transactionId));
            
        } catch (Exception e) {
            LOGGER.severe(String.format("[TX:%s] Database rollback error: %s", transactionId, e.getMessage()));
        }
    }

    @Override
    public void rollbackMinIO(MinioClient client) {
        try {
            if (minioFileUploaded && minioFileName != null) {
                LOGGER.info(String.format("[TX:%s] Rolling back MinIO: deleting file %s", transactionId, minioFileName));
                minioService.deleteFile(minioFileName);
                LOGGER.info(String.format("[TX:%s] MinIO rollback successful", transactionId));
            }
        } catch (Exception e) {
            LOGGER.severe(String.format("[TX:%s] MinIO rollback error: %s", transactionId, e.getMessage()));
        }
    }
}
