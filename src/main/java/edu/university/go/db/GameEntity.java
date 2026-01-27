package edu.university.go.db;

import edu.university.go.game.PlayerType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Game instance in db
 */
@Entity
@Data
@Table(name = "games")
public class GameEntity {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer boardSize;
    
    // Player types (Bot/Human)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType blackType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType whiteType;
    
    @Column(nullable = false)
    private String status; // ACTIVE, FINISHED, ABANDONED
    
    private String winner; // BLACK, WHITE, null
    private Double blackScore;
    private Double whiteScore;
    
    @Column(nullable = false)
    private Integer capturedByBlack = 0;
    
    @Column(nullable = false)
    private Integer capturedByWhite = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("turnNumber ASC")
    private List<MoveEntity> moves = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (capturedByBlack == null) {
            capturedByBlack = 0;
        }
        if (capturedByWhite == null) {
            capturedByWhite = 0;
        }
    }
    
    /**
     * Automatic update of updatedAt field on update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add move helper method
     */
    public void addMove(MoveEntity move) {
        moves.add(move);
        move.setGame(this);
    }
    
    /**
     * Helper method to remove move
     */
    public void removeMove(MoveEntity move) {
        moves.remove(move);
        move.setGame(null);
    }
    
    /**
     * Gets total number of moves
     */
    public int getTotalMoves() {
        return moves.size();
    }
    
    public boolean isFinished() {
        return "FINISHED".equals(status) || "ABANDONED".equals(status);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    @Override
    public String toString() {
        return "GameEntity{" +
                "id=" + id +
                ", boardSize=" + boardSize +
                ", blackType=" + blackType +
                ", whiteType=" + whiteType +
                ", status='" + status + '\'' +
                ", winner='" + winner + '\'' +
                ", blackScore=" + blackScore +
                ", whiteScore=" + whiteScore +
                ", capturedByBlack=" + capturedByBlack +
                ", capturedByWhite=" + capturedByWhite +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}