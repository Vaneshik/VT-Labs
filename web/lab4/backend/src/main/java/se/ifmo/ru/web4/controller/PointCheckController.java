package se.ifmo.ru.web4.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.ifmo.ru.web4.model.Point;
import se.ifmo.ru.web4.service.PointCheckService;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointCheckController {
    private final PointCheckService service;

    public PointCheckController(PointCheckService service) {
        this.service = service;
    }

    @Setter
    @Getter
    public static class PointCheckRequest {
        private double x;
        private double y;
        private double r;
    }

    @PostMapping
    public ResponseEntity<?> submitPoint(@RequestBody PointCheckRequest request) {
        try {
            Point result = service.checkAndSavePoint(request.getX(), request.getY(), request.getR());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Point>> getAllPoints() {
        List<Point> points = service.getAllPointChecks();
        return ResponseEntity.ok(points);
    }
}