package se.ifmo.ru.web4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.ifmo.ru.web4.model.Point;

import java.util.List;

@Repository
public interface PointCheckRepository extends JpaRepository<Point, Long> {
    List<Point> findByUserId(Long userId);
}