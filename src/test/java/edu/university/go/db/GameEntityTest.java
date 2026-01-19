package edu.university.go.db;

import edu.university.go.game.PlayerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEntityTest {

    @Test
    void testGettersAndSetters() {
        GameEntity entity = new GameEntity();
        entity.setId(1L);
        entity.setBlackType(PlayerType.HUMAN);
        entity.setWhiteType(PlayerType.BOT);

        assertEquals(1L, entity.getId());
        assertEquals(PlayerType.HUMAN, entity.getBlackType());
        assertEquals(PlayerType.BOT, entity.getWhiteType());
    }

    @Test
    void testEqualsAndHashCode() {
        GameEntity entity1 = new GameEntity();
        entity1.setId(1L);
        entity1.setBlackType(PlayerType.HUMAN);
        entity1.setWhiteType(PlayerType.BOT);

        GameEntity entity2 = new GameEntity();
        entity2.setId(1L);
        entity2.setBlackType(PlayerType.HUMAN);
        entity2.setWhiteType(PlayerType.BOT);

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());

        entity2.setId(2L);
        assertNotEquals(entity1, entity2);
    }

    @Test
    void testToString() {
        GameEntity entity = new GameEntity();
        entity.setId(1L);
        entity.setBlackType(PlayerType.HUMAN);
        entity.setWhiteType(PlayerType.BOT);

        String toString = entity.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("blackType=HUMAN"));
        assertTrue(toString.contains("whiteType=BOT"));
    }
}