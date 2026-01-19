package edu.university.go.game;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import java.util.Set;

class InProgress implements GameState {

  private final Set<String> players;
  private int consecutivePasses = 0;

  InProgress(Set<String> players) {
    this.players = players;
  }

  @Override
  public void addPlayer(Game game, String playerId) {
    throw new IllegalStateException("Game already started");
  }

  @Override
  public void makeMove(Game game, Move move) {

    if (!players.contains(move.playerId())) {
      throw new IllegalArgumentException("Unknown player");
    }

    if (move.color() != game.getCurrentTurn()) {
      throw new IllegalStateException("Not your turn");
    }

    // Handle PASS and RESIGN moves
    if (move.isPass()) {
      consecutivePasses++;
      if (move.color() == Color.BLACK) {
        game.blackPassStones++;
      } else {
        game.whitePassStones++;
      }
      game.switchTurn();
      game.notifyObservers(GameEvent.MOVE_PLAYED);

      // Check if game should end after 2 consecutive passes
      if (consecutivePasses >= 2) {
        game.notifyObservers(GameEvent.GAME_ENDED);
      }

      return;
    }

    if (move.isResign()) {
      game.notifyObservers(GameEvent.MOVE_PLAYED);
      game.notifyObservers(GameEvent.GAME_ENDED);
      // Game ends, no turn switch needed
      return;
    }

    // Reset consecutive passes on regular stone placement
    consecutivePasses = 0;

    // Handle regular stone placement
    Board board = game.getBoard();

    // Clone the board before the move for Ko rule updates
    Board boardBefore = board.clone();

    // Check Ko rule violation before placing the stone
    if (game.getKoRule().isKoViolation(board, move.x(), move.y(), move.color())) {
      throw new IllegalStateException("Ko rule violation");
    }

    boolean placed = board.placeStone(move.color(), move.x(), move.y());
    if (!placed) {
      // placement failed (out of bounds, occupied, or suicide) -> reject move
      throw new IllegalStateException("Invalid move");
    }

    // Update Ko rule state after successful move
    game.getKoRule().updateState(boardBefore);
    if (move.color() == Color.BLACK) {
      game.blackPlacedStones++;
    } else {
      game.whitePlacedStones++;
    }
    game.switchTurn();
    game.notifyObservers(GameEvent.MOVE_PLAYED);
  }
}
