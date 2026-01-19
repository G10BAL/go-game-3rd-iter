package edu.university.go.scoring;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TerritoryCounterTest {

  private TerritoryCounter counter;
  private Board board;

  @BeforeEach
  void setUp() {
    counter = new TerritoryCounter();
    board = new Board(9);
  }

  @Test
  @DisplayName("Empty board has no territory")
  void testEmptyBoardHasNoTerritory() {
    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertEquals(0, territory.get(Color.BLACK), "Black has no territory on empty board");
    assertEquals(0, territory.get(Color.WHITE), "White has no territory on empty board");
  }

  @Test
  @DisplayName("One black stone in corner")
  void testSingleBlackStoneInCorner() {
    /*
     * B . . .
     * . . . .
     * . . . .
     */
    board.placeStone(Color.BLACK, 0, 0);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertTrue(territory.get(Color.BLACK) > 0, "Black has territory");
    assertEquals(0, territory.get(Color.WHITE), "White has no territory");
  }

  @Test
  @DisplayName("Simple black territory")
  void testSimpleBlackTerritory() {
    /*
     * B B B .
     * B . B .
     * B B B .
     * . . . W
     */
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, 2);
      board.placeStone(Color.WHITE, 3, 3);
    }

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertEquals(1, territory.get(Color.BLACK), "Black has 1 point of territory");
    assertEquals(0, territory.get(Color.WHITE), "White has no territory");
  }

  @Test
  @DisplayName("Simple white territory")
  void testSimpleWhiteTerritory() {
    /*
     * W W W .
     * W . W .
     * W W W .
     * . . . B
     */
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.WHITE, 0, i);
      board.placeStone(Color.WHITE, 2, i);
      board.placeStone(Color.WHITE, i, 0);
      board.placeStone(Color.WHITE, i, 2);
      board.placeStone(Color.BLACK, 3, 3);
    }

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertEquals(0, territory.get(Color.BLACK), "Black has no territory");
    assertEquals(1, territory.get(Color.WHITE), "White has 1 point of territory");
  }

  @Test
  @DisplayName("Two separate black territories")
  void testTwoSeparateBlackTerritories() {
    /*
     * B B . . W W
     * B . B . W . W
     * B B . . W W
     */
    // First black territory
    board.placeStone(Color.BLACK, 0, 0);
    board.placeStone(Color.BLACK, 0, 1);
    board.placeStone(Color.BLACK, 1, 0);
    board.placeStone(Color.BLACK, 1, 2);
    board.placeStone(Color.BLACK, 2, 0);
    board.placeStone(Color.BLACK, 2, 1);

    // Second white territory
    board.placeStone(Color.WHITE, 0, 4);
    board.placeStone(Color.WHITE, 0, 5);
    board.placeStone(Color.WHITE, 1, 4);
    board.placeStone(Color.WHITE, 1, 6);
    board.placeStone(Color.WHITE, 2, 4);
    board.placeStone(Color.WHITE, 2, 5);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertTrue(territory.get(Color.BLACK) >= 1, "Black has at least 1 point");
    assertTrue(territory.get(Color.WHITE) >= 1, "White has at least 1 point");
  }

  @Test
  @DisplayName("Neutral territory (surrounded by both colors)")
  void testNeutralTerritory() {
    /*
     * B W
     * . .
     * W B
     */
    board.placeStone(Color.BLACK, 0, 0);
    board.placeStone(Color.WHITE, 0, 1);
    board.placeStone(Color.WHITE, 2, 0);
    board.placeStone(Color.BLACK, 2, 1);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    int totalClaimed = territory.get(Color.BLACK) + territory.get(Color.WHITE);
    int emptyPoints = countEmptyPoints();

    assertTrue(
        totalClaimed < emptyPoints, "Not all empty points are claimed (there are neutral ones)");
  }

  @Test
  @DisplayName("Large black territory")
  void testLargeBlackTerritory() {
    /*
     * Create a large 5x5 territory
     * B B B B B B B .
     * B . . . . . B .
     * B . . . . . B .
     * B . . . . . B .
     * B . . . . . B .
     * B . . . . . B .
     * B B B B B B B .
     * . . . . . . . W
     */
    int size = 7;
    for (int i = 0; i < size; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, size - 1, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, size - 1);
      board.placeStone(Color.WHITE, 7, 7);
    }

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    int expectedTerritory = (size - 2) * (size - 2); // 5 * 5 = 25
    assertEquals(
        expectedTerritory,
        territory.get(Color.BLACK),
        "Black has " + expectedTerritory + " points of territory");
    assertEquals(0, territory.get(Color.WHITE), "White has no territory");
  }

  @Test
  @DisplayName("Complex position with multiple territories")
  void testComplexPositionWithMultipleTerritories() {
    /*
     * B B B . W W W
     * B . B . W . W
     * B B B . W W W
     * . . . . . . .
     * B B . . W W .
     * B . B . W . W
     * B B . . W W .
     */

    // First territory
    createTerritory(0, 0, 3, Color.BLACK);

    // Second territory
    createTerritory(0, 4, 3, Color.WHITE);

    // Third territory
    createTerritory(4, 0, 3, Color.BLACK);

    // Fourth territory
    createTerritory(4, 4, 3, Color.WHITE);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertTrue(territory.get(Color.BLACK) >= 2, "Black has at least 2 points");
    assertTrue(territory.get(Color.WHITE) >= 2, "White has at least 2 points");
  }

  @Test
  @DisplayName("Territory at board edge")
  void testTerritoryAtBoardEdge() {
    /*
     * B B B
     * B . .
     * B . .
     */
    board.placeStone(Color.BLACK, 0, 0);
    board.placeStone(Color.BLACK, 0, 1);
    board.placeStone(Color.BLACK, 0, 2);
    board.placeStone(Color.BLACK, 1, 0);
    board.placeStone(Color.BLACK, 2, 0);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertTrue(territory.get(Color.BLACK) >= 4, "Black has territory at the board edge");
  }

  @Test
  @DisplayName("Territory in corner")
  void testTerritoryInCorner() {
    /*
     * B B .
     * B . .
     * . . .
     */
    board.placeStone(Color.BLACK, 0, 0);
    board.placeStone(Color.BLACK, 0, 1);
    board.placeStone(Color.BLACK, 1, 0);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertTrue(territory.get(Color.BLACK) >= 1, "Black has territory in the corner");
  }

  @Test
  @DisplayName("No territory with even distribution")
  void testNoTerritoryWithEvenDistribution() {
    /*
     * B W B W
     * W B W B
     * B W B W
     * W B W B
     */
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        Color color = ((i + j) % 2 == 0) ? Color.BLACK : Color.WHITE;
        board.placeStone(color, i, j);
      }
    }

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertEquals(0, territory.get(Color.BLACK), "Black has no territory with even distribution");
    assertEquals(5, territory.get(Color.WHITE), "White has 5, as they have taken 5 stones");
  }

  @Test
  @DisplayName("Complex territory with 'eyes'")
  void testComplexTerritoryWithEyes() {
    /*
     * Create a group with two 'eyes'
     * B B B B B
     * B . B . B
     * B B B B B
     */
    // Зовнішня межа
    for (int i = 0; i < 5; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
    }
    board.placeStone(Color.BLACK, 1, 0);
    board.placeStone(Color.BLACK, 1, 2);
    board.placeStone(Color.BLACK, 1, 4);
    board.placeStone(Color.WHITE, 5, 5);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertEquals(2, territory.get(Color.BLACK), "Black has 2 points of territory (two 'eyes')");
  }

  @Test
  @DisplayName("Territory not counted if there is a stone inside")
  void testNoTerritoryWithStoneInside() {
    /*
     * B B B
     * B B B
     * B B B
     */
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, 2);
    }
    board.placeStone(Color.BLACK, 1, 1);

    board.placeStone(Color.WHITE, 3, 3);

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    assertEquals(
        0, territory.get(Color.BLACK), "Black has no territory if there is a black stone inside");
    assertEquals(0, territory.get(Color.WHITE), "No territory");
  }

  @Test
  @DisplayName("Підрахунок на повній дошці 9x9")
  void testFullBoard9x9() {
    // Devide the board into two halves
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 4; j++) {
        board.placeStone(Color.BLACK, i, j);
      }
      for (int j = 5; j < 9; j++) {
        board.placeStone(Color.WHITE, i, j);
      }
    }

    Map<Color, Integer> territory = counter.calculateTerritory(board);

    // Middle column should have no territory
    int totalTerritory = territory.get(Color.BLACK) + territory.get(Color.WHITE);
    assertTrue(totalTerritory <= 9, "Максимум 9 точок території (середній стовпець)");
  }

  // Helper method to create a square territory

  /** Create a square territory of given size and color starting from (startX, startY) */
  private void createTerritory(int startX, int startY, int size, Color color) {
    for (int i = 0; i < size; i++) {
      if (startX + i < board.getSize() && startY < board.getSize()) {
        board.placeStone(color, startX, startY + i);
        board.placeStone(color, startX + size - 1, startY + i);
      }
      if (startX < board.getSize() && startY + i < board.getSize()) {
        board.placeStone(color, startX + i, startY);
        board.placeStone(color, startX + i, startY + size - 1);
      }
    }
  }

  /** Count empty points on the board */
  private int countEmptyPoints() {
    int count = 0;
    for (int i = 0; i < board.getSize(); i++) {
      for (int j = 0; j < board.getSize(); j++) {
        if (board.get(i, j) == Color.EMPTY) {
          count++;
        }
      }
    }
    return count;
  }
}
