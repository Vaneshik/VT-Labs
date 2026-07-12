package se.ifmo.ru.web4.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Component
@Entity
@Table(name = "point_model")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double x;
    private double y;
    private double r;
    private boolean hit;
    private LocalDateTime checkTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Point(double x, double y, double r, boolean hit, LocalDateTime checkTime, User user) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.checkTime = checkTime;
        this.user = user;
    }
}