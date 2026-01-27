package edu.university.go.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoveRepository extends JpaRepository<MoveEntity, Long> {
    
    /**
     * Find all turns for a specific game
     */
    List<MoveEntity> findByGameIdOrderByTurnNumberAsc(Long gameId);
    
    /**
     * Count the number of moves in a specific game
     */
    int countByGameId(Long gameId);
}