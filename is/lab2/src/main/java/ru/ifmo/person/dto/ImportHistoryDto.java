package ru.ifmo.person.dto;

import ru.ifmo.person.enumeration.ImportStatus;
import java.util.Date;

public class ImportHistoryDto {
    private Long id;
    private ImportStatus status;
    private Date createdAt;
    private String username;
    private Integer importedCount;
    private String errorMessage;

    public ImportHistoryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(Integer importedCount) {
        this.importedCount = importedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
