package edu.university.go.validators;

import edu.university.go.board.Board;
import edu.university.go.board.Color;

/**
 * Ko Rule Validator
 *
 * <p>The Ko rule prevents immediate recapture that recreates the previous board state. We store the
 * board state BEFORE each move, then after a move is made, we check if the new board state matches
 * the board state from TWO moves ago.
 */
public class KoRule {

  private Board boardBeforePreviousMove; // Board state before the move 2 turns ago

  /**
   * Check if placing a stone at (x, y) would violate the Ko rule. This simulates the placement and
   * checks if the resulting board matches the board from TWO moves ago (immediate repeat of
   * position).
   *
   * @param currentBoard The board before the move
   * @param x Row coordinate
   * @param y Column coordinate
   * @param color Stone color to place
   * @return true if Ko violation, false otherwise
   */
  public boolean isKoViolation(Board currentBoard, int x, int y, Color color) {
    if (boardBeforePreviousMove == null) {
      return false;
    }

    // Simulate the move
    Board simulated = currentBoard.clone();
    boolean placed = simulated.placeStone(color, x, y);

    if (!placed) {
      return false; // Move is invalid anyway
    }

    // Check if the board after this move matches the board from 2 moves ago
    // If yes, this would be an immediate recapture (Ko violation)
    return simulated.equals(boardBeforePreviousMove);
  }

  /**
   * Update the Ko rule state after a valid move has been made. This should be called BEFORE the
   * next move is attempted.
   *
   * <p>We shift the board states: - boardBeforePreviousMove {@literal <-} current board
   *
   * @param boardBeforeThisMove The board state BEFORE the move that just happened
   */
  public void updateState(Board boardBeforeThisMove) {
    // The board before this move becomes the reference for Ko detection
    // (it will be the board from 2 moves ago after the opponent moves)
    this.boardBeforePreviousMove = boardBeforeThisMove.clone();
  }

  /** Reset Ko rule state (e.g., at game start) */
  public void reset() {
    this.boardBeforePreviousMove = null;
  }
}
