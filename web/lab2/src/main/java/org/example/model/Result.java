package org.example.model;

import java.time.LocalDateTime;

public class Result {
    private static long counter = 1; // Глобальный счетчик ID
    private final long id; // Уникальный ID для каждой записи
    private final double x;
    private final double y;
    private final double r;
    private final boolean isHit;
    private final LocalDateTime startTime;
    private final long executionTime;

    public Result(double x, double y, double r, boolean isHit, LocalDateTime startTime, long executionTime) {
        this.id = counter++; // Автоматическое увеличение ID
        this.x = x;
        this.y = y;
        this.r = r;
        this.isHit = isHit;
        this.startTime = startTime;
        this.executionTime = executionTime;
    }

    public long getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public boolean isHit() {
        return isHit;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }
}