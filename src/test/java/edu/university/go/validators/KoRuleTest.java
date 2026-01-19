package edu.university.go.validators;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for Ko Rule validation.
 *
 * <p>Ko rule prevents immediate recapture that recreates the previous board state. The key
 * principle: after a capture, the opponent cannot immediately recapture if it would return the
 * board to the state before the original capture.
 */
class KoRuleTest {

  private KoRule validator;
  private Board board;

  @BeforeEach
  void setUp() {
    validator = new KoRule();
    board = new Board(9);
  }

  @Test
  @DisplayName("First move cannot be Ko violation")
  void testFirstMoveCannotBeKoViolation() {
    boolean isKo = validator.isKoViolation(board, 4, 4, Color.BLACK);
    assertFalse(isKo, "First move cannot be Ko violation");
  }

  @Test
  @DisplayName("Second move cannot be Ko violation")
  void testSecondMoveCannotBeKoViolation() {
    // Black plays first
    Board boardBefore = board.clone();
    board.placeStone(Color.BLACK, 4, 4);
    validator.updateState(boardBefore);

    // White plays second
    boolean isKo = validator.isKoViolation(board, 5, 5, Color.WHITE);
    assertFalse(isKo, "Second move cannot be Ko violation");
  }

  @Test
  @DisplayName("Simple Ko scenario - capture and attempted return")
  void testSimpleKoScenario() {

    Board boardMove0 = board.clone();

    board.placeStone(Color.BLACK, 0, 1); // Move 1
    Board boardMove1 = board.clone();
    validator.updateState(boardMove0); // Save state before move 1

    board.placeStone(Color.WHITE, 0, 2); // Move 2
    Board boardMove2 = board.clone();
    validator.updateState(boardMove1); // Save state before move 2

    board.placeStone(Color.BLACK, 2, 1); // Move 3
    Board boardMove3 = board.clone();
    validator.updateState(boardMove2); // Save state before move 3

    board.placeStone(Color.WHITE, 2, 2); // Move 4
    Board boardMove4 = board.clone();
    validator.updateState(boardMove3); // Save state before move 4

    board.placeStone(Color.BLACK, 1, 0); // Move 5
    Board boardMove5 = board.clone();
    validator.updateState(boardMove4); // Save state before move 5

    board.placeStone(Color.WHITE, 1, 1); // Move 6
    Board boardMove6 = board.clone();
    validator.updateState(boardMove5); // Save state before move 6

    board.placeStone(Color.BLACK, 4, 4); // Move 7
    Board boardMove7 = board.clone();
    validator.updateState(boardMove6); // Save state before move 7

    board.placeStone(Color.WHITE, 1, 3); // Move 8
    Board boardMove8 = board.clone();
    validator.updateState(boardMove7); // Save state before move 8

    // Now Black makes a capture move at (1, 2)
    board.placeStone(Color.BLACK, 1, 2); // Move 9 - captures White at (1,1)
    Board boardAfterCapture = board.clone();
    validator.updateState(boardMove8); // Save state before the capture move

    // White tries to immediately recapture at the same spot
    // This would recreate boardMove6 (the state before Black's capture)
    boolean isKo = validator.isKoViolation(boardAfterCapture, 1, 1, Color.WHITE);
    assertTrue(isKo, "Immediate recapture should violate Ko rule");
  }

  @Test
  @DisplayName("Ko violation is prevented only for immediate recapture")
  void testKoViolationOnlyForImmediateRecapture() {
    // Same setup as before, but Black plays elsewhere before White tries to recapture
    Board boardMove0 = board.clone();

    board.placeStone(Color.BLACK, 0, 1);
    Board boardMove1 = board.clone();
    validator.updateState(boardMove0);

    board.placeStone(Color.WHITE, 0, 2);
    Board boardMove2 = board.clone();
    validator.updateState(boardMove1);

    board.placeStone(Color.BLACK, 1, 0);
    Board boardMove3 = board.clone();
    validator.updateState(boardMove2);

    board.placeStone(Color.WHITE, 1, 1);
    Board boardMove4 = board.clone();
    validator.updateState(boardMove3);

    board.placeStone(Color.BLACK, 1, 2);
    Board boardMove5 = board.clone();
    validator.updateState(boardMove4);

    board.placeStone(Color.WHITE, 2, 1);
    Board boardMove6 = board.clone();
    validator.updateState(boardMove5);

    // Black captures at (1, 1)
    board.placeStone(Color.BLACK, 1, 1);
    Board boardAfterCapture = board.clone();
    validator.updateState(boardMove6);

    // Black plays elsewhere
    board.placeStone(Color.BLACK, 3, 3);
    Board boardMove8 = board.clone();
    validator.updateState(boardAfterCapture);

    // NOW White can recapture at (1, 1) - no Ko violation because it's not immediate
    boolean isKo = validator.isKoViolation(boardMove8, 1, 1, Color.WHITE);
    assertFalse(isKo, "Recapture after opponent plays elsewhere should not violate Ko");
  }
}
