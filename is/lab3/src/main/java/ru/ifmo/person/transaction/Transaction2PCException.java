package ru.ifmo.person.transaction;

public class Transaction2PCException extends Exception {
    
    public Transaction2PCException(String message) {
        super(message);
    }
    
    public Transaction2PCException(String message, Throwable cause) {
        super(message, cause);
    }
}
