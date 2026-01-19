package edu.university.go.board;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BoardTest {

  @Test
  void emptyBoard_hasAllEmptyIntersections() {
    Board board = new Board(9);

    for (int x = 0; x < 9; x++) {
      for (int y = 0; y < 9; y++) {
        assertEquals(Color.EMPTY, board.get(x, y));
      }
    }
  }

  @Test
  void placeStone_onEmptyIntersection_succeeds() {
    Board board = new Board(9);

    boolean result = board.placeStone(Color.BLACK, 4, 4);

    assertTrue(result);
    assertEquals(Color.BLACK, board.get(4, 4));
  }

  @Test
  void placeStone_onOccupiedIntersection_fails() {
    Board board = new Board(9);

    assertTrue(board.placeStone(Color.BLACK, 4, 4));
    assertFalse(board.placeStone(Color.WHITE, 4, 4));
    assertEquals(Color.BLACK, board.get(4, 4));
  }

  @Test
  void capture_singleStone() {
    Board board = new Board(5);

    // Black stone
    board.placeStone(Color.BLACK, 2, 2);

    // surround with white
    board.placeStone(Color.WHITE, 2, 1);
    board.placeStone(Color.WHITE, 1, 2);
    board.placeStone(Color.WHITE, 3, 2);
    board.placeStone(Color.WHITE, 2, 3);

    assertEquals(Color.EMPTY, board.get(2, 2), "Surrounded stone should be captured");
  }

  @Test
  void capture_chainOfTwoStones() {
    Board board = new Board(5);

    // Black chain
    board.placeStone(Color.BLACK, 2, 2);
    board.placeStone(Color.BLACK, 2, 3);

    // surround with white
    board.placeStone(Color.WHITE, 1, 2);
    board.placeStone(Color.WHITE, 3, 2);
    board.placeStone(Color.WHITE, 2, 1);

    board.placeStone(Color.WHITE, 1, 3);
    board.placeStone(Color.WHITE, 3, 3);
    board.placeStone(Color.WHITE, 2, 4);

    assertEquals(Color.EMPTY, board.get(2, 2));
    assertEquals(Color.EMPTY, board.get(2, 3));
  }

  @Test
  void stoneWithLiberty_isNotCaptured() {
    Board board = new Board(5);

    board.placeStone(Color.BLACK, 2, 2);

    board.placeStone(Color.WHITE, 1, 2);
    board.placeStone(Color.WHITE, 3, 2);
    board.placeStone(Color.WHITE, 2, 1);
    // liberty at (2,3)

    assertEquals(Color.BLACK, board.get(2, 2));
  }

  @Test
  void suicideMove_isRejected() {
    Board board = new Board(5);

    board.placeStone(Color.WHITE, 2, 1);
    board.placeStone(Color.WHITE, 1, 2);
    board.placeStone(Color.WHITE, 3, 2);
    board.placeStone(Color.WHITE, 2, 3);

    boolean result = board.placeStone(Color.BLACK, 2, 2);

    assertFalse(result, "Suicide move should be rejected");
    assertEquals(Color.EMPTY, board.get(2, 2));
  }

  @Test
  void suicideThatCaptures_isAllowed() {
    Board board = new Board(5);

    // White stone
    board.placeStone(Color.WHITE, 2, 2);

    // surround with black
    board.placeStone(Color.BLACK, 2, 1);
    board.placeStone(Color.BLACK, 1, 2);
    board.placeStone(Color.BLACK, 3, 2);

    // this move looks like suicide,
    // but it takes the white stone
    boolean result = board.placeStone(Color.BLACK, 2, 3);

    assertTrue(result);
    assertEquals(Color.EMPTY, board.get(2, 2));
    assertEquals(Color.BLACK, board.get(2, 3));
  }
}
