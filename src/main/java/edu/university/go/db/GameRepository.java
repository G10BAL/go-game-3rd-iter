package edu.university.go.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
    
    /**
     * Updates game status and related fields
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE GameEntity g SET " +
           "g.status = :status, " +
           "g.winner = :winner, " +
           "g.blackScore = :blackScore, " +
           "g.whiteScore = :whiteScore, " +
           "g.capturedByBlack = :capturedByBlack, " +
           "g.capturedByWhite = :capturedByWhite, " +
           "g.updatedAt = :updatedAt " +
           "WHERE g.id = :gameId")
    void updateGameStatus(
        @Param("gameId") Long gameId,
        @Param("status") String status,
        @Param("winner") String winner,
        @Param("blackScore") Double blackScore,
        @Param("whiteScore") Double whiteScore,
        @Param("capturedByBlack") Integer capturedByBlack,
        @Param("capturedByWhite") Integer capturedByWhite,
        @Param("updatedAt") LocalDateTime updatedAt
    );
}