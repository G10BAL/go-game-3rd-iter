package edu.university.go.db;

import edu.university.go.game.PlayerType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class GameEntity {
    @Id @GeneratedValue
    private Long id;
    private PlayerType blackType;
    private PlayerType whiteType;
}