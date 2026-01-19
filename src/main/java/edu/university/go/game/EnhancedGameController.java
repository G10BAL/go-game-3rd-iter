package edu.university.go.game;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.board.Point;
import edu.university.go.scoring.GameResult;
import edu.university.go.scoring.ScoreCalculator;
import edu.university.go.validators.KoRule;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing the state of a Go game with enhanced features: - Ko rule enforcement -
 * Score calculation with komi - Captured stones tracking - Game end conditions (two consecutive
 * passes or resignation)
 */
public class EnhancedGameController {

  private final Game game;
  private final Board board;
  private final KoRule koValidator;
  private final ScoreCalculator scoreCalculator;

  private int capturedByBlack = 0;
  private int capturedByWhite = 0;
  private int consecutivePasses = 0;
  private boolean gameEnded = false;

  private String blackPlayerId = "player-black";
  private String whitePlayerId = "player-white";

  private final List<GameObserver> observers = new ArrayList<>();

  public EnhancedGameController(int boardSize, double komi) {
    this.board = new Board(boardSize);
    this.game = new Game(board);
    this.koValidator = new KoRule();
    this.scoreCalculator = new ScoreCalculator(komi);
  }

  /** Try to make a move at (x, y) */
  public boolean makeMove(int x, int y) {
    if (gameEnded) {
      return false;
    }

    Color currentColor = game.getCurrentTurn();

    // Save board state before the move for Ko rule validation
    Board boardBeforeMove = board.clone();

    // Check for Ko violation
    if (koValidator.isKoViolation(board, x, y, currentColor)) {
      System.out.println("[Ko Rule] Move (" + x + ", " + y + ") violates Ko rule!");
      notifyObservers(GameEvent.INVALID_MOVE);
      return false;
    }

    // Get the stones that will be captured
    List<Point> captured = board.getCapturedStones(currentColor, x, y);
    System.out.println(
        "["
            + currentColor
            + " move at ("
            + x
            + ", "
            + y
            + ")] Will capture: "
            + captured.size()
            + " stones");

    // Get the player ID for the current color
    String playerId = (currentColor == Color.BLACK) ? blackPlayerId : whitePlayerId;

    // Try to place the stone
    Move move = new Move(currentColor, x, y, playerId, MoveType.PLACE_STONE);

    try {
      game.makeMove(move);

      // Update captured stones counter
      if (!captured.isEmpty()) {
        System.out.println(
            "[" + currentColor + " move] Actually captured " + captured.size() + " stones!");
        if (currentColor == Color.BLACK) {
          capturedByBlack += captured.size();
          System.out.println("Total BLACK captured: " + capturedByBlack);
        } else {
          capturedByWhite += captured.size();
          System.out.println("Total WHITE captured: " + capturedByWhite);
        }
      }

      // Update state for Ko after move succeeded (pass the board BEFORE the move)
      koValidator.updateState(boardBeforeMove);

      // Reset passes
      consecutivePasses = 0;

      notifyObservers(GameEvent.MOVE_PLAYED);
      return true;

    } catch (IllegalStateException e) {
      notifyObservers(GameEvent.INVALID_MOVE);
      return false;
    }
  }

  /** Pass (skip the turn) */
  public void pass() {
    if (gameEnded) {
      return;
    }

    consecutivePasses++;

    // Two passes
    if (consecutivePasses >= 2) {
      endGame();
    } else {
      game.switchTurn();
      notifyObservers(GameEvent.MOVE_PLAYED);
    }
  }

  /** Resign */
  public void resign() {
    gameEnded = true;
    // Winner is the opposite color
    Color winner = game.getCurrentTurn().opposite();
    notifyObservers(GameEvent.GAME_ENDED);
  }

  /** End the game and calculate the score */
  private void endGame() {
    gameEnded = true;
    notifyObservers(GameEvent.GAME_ENDED);
  }

  /** Get the game result */
  public GameResult getGameResult() {
    return scoreCalculator.calculateScore(board, capturedByBlack, capturedByWhite);
  }

  // Getters
  public Board getBoard() {
    return board;
  }

  public Game getGame() {
    return game;
  }

  public Color getCurrentTurn() {
    return game.getCurrentTurn();
  }

  public int getCapturedByBlack() {
    return capturedByBlack;
  }

  public int getCapturedByWhite() {
    return capturedByWhite;
  }

  public boolean isGameEnded() {
    return gameEnded;
  }

  // Setters for player IDs
  public void setBlackPlayerId(String playerId) {
    this.blackPlayerId = playerId;
  }

  public void setWhitePlayerId(String playerId) {
    this.whitePlayerId = playerId;
  }

  // Observer pattern
  public void addObserver(GameObserver observer) {
    observers.add(observer);
  }

  private void notifyObservers(GameEvent event) {
    observers.forEach(o -> o.onGameEvent(event));
  }
}
