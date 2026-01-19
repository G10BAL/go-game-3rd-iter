package edu.university.go.db;

import edu.university.go.board.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveEntityTest {

    @Test
    void testGettersAndSetters() {
        MoveEntity entity = new MoveEntity();
        GameEntity game = new GameEntity();
        entity.setId(1L);
        entity.setGame(game);
        entity.setTurnNumber(5);
        entity.setTurnType("MOVE");
        entity.setColor(Color.BLACK);
        entity.setX(3);
        entity.setY(4);

        assertEquals(1L, entity.getId());
        assertEquals(game, entity.getGame());
        assertEquals(5, entity.getTurnNumber());
        assertEquals("MOVE", entity.getTurnType());
        assertEquals(Color.BLACK, entity.getColor());
        assertEquals(3, entity.getX());
        assertEquals(4, entity.getY());
    }

    @Test
    void testEqualsAndHashCode() {
        MoveEntity entity1 = new MoveEntity();
        entity1.setId(1L);
        entity1.setTurnNumber(5);
        entity1.setTurnType("MOVE");
        entity1.setColor(Color.BLACK);

        MoveEntity entity2 = new MoveEntity();
        entity2.setId(1L);
        entity2.setTurnNumber(5);
        entity2.setTurnType("MOVE");
        entity2.setColor(Color.BLACK);

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());

        entity2.setId(2L);
        assertNotEquals(entity1, entity2);
    }

    @Test
    void testToString() {
        MoveEntity entity = new MoveEntity();
        entity.setId(1L);
        entity.setTurnNumber(5);
        entity.setTurnType("MOVE");
        entity.setColor(Color.BLACK);
        entity.setX(3);
        entity.setY(4);

        String toString = entity.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("turnNumber=5"));
        assertTrue(toString.contains("turnType=MOVE"));
        assertTrue(toString.contains("color=BLACK"));
        assertTrue(toString.contains("x=3"));
        assertTrue(toString.contains("y=4"));
    }
}