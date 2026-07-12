package ru.ifmo.person.transaction;

public class PrepareResult {
    private final boolean success;
    private final String message;
    private final Exception exception;

    private PrepareResult(boolean success, String message, Exception exception) {
        this.success = success;
        this.message = message;
        this.exception = exception;
    }

    public static PrepareResult success() {
        return new PrepareResult(true, "Prepared successfully", null);
    }

    public static PrepareResult success(String message) {
        return new PrepareResult(true, message, null);
    }

    public static PrepareResult failure(String message) {
        return new PrepareResult(false, message, null);
    }

    public static PrepareResult failure(String message, Exception exception) {
        return new PrepareResult(false, message, exception);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "PrepareResult{success=" + success + ", message='" + message + "'}";
    }
}
