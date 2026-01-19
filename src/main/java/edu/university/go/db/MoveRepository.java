package edu.university.go.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoveRepository extends JpaRepository<MoveEntity, Long> {
    List<MoveEntity> findByGameIdOrderByTurnNumberAsc(Long gameId);
}