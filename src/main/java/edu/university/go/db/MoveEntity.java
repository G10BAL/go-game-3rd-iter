package edu.university.go.db;

import edu.university.go.board.Color;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class MoveEntity {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private GameEntity game;
    private int turnNumber;
    private String turnType; // (MOVE,PASS,RESIGN)
    private Color color;
    private Integer x; // null for PASS/RESIGN
    private Integer y; // null for PASS/RESIGN
}