package se.ifmo.ru.web4.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.ifmo.ru.web4.model.Point;
import se.ifmo.ru.web4.model.User;
import se.ifmo.ru.web4.repository.PointCheckRepository;
import se.ifmo.ru.web4.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointCheckService {
    private final PointCheckRepository pointRepository;
    private final UserRepository userRepository;

    public PointCheckService(PointCheckRepository pointRepository, UserRepository userRepository) {
        this.pointRepository = pointRepository;
        this.userRepository = userRepository;
    }

    public boolean checkHit(double x, double y, double r) {
//        if (x < -5 || x > 5)
//            throw new IllegalArgumentException("X must be between -5 and 5");
//        if (y < -5 || y > 3)
//            throw new IllegalArgumentException("Y must be between -5 and 3");
//        if (r <= 0 || r > 5)
//            throw new IllegalArgumentException("R must be > 0 and <= 5");

        boolean hit;
        if (x <= 0 && y >= 0) {
            hit = (x >= -r) && (y <= r) && (r >= y - x);
        } else if (x >= 0 && y >= 0) {
            hit = (x <= r) && (y <= r / 2d);
        } else if (x >= 0 && y <= 0) {
            hit = false;
        } else {
            hit = (x * x + y * y <= r * r);
        }

        return hit;
    }

    public Point checkAndSavePoint(double x, double y, double r) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        boolean hit = checkHit(x, y, r);
        Point pointCheck = new Point(x, y, r, hit, LocalDateTime.now(), user);
        return pointRepository.save(pointCheck);
    }

    public List<Point> getAllPointChecks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) Найти все точки конкретного пользователя
        List<Point> points = pointRepository.findByUserId(user.getId());

        return ResponseEntity.ok(points).getBody();
    }
}