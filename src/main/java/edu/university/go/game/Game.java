package edu.university.go.game;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.validators.KoRule;
import java.util.ArrayList;
import java.util.List;

public class Game {

  private final Board board;
  private GameState state;
  private Color currentTurn = Color.BLACK;
  private final KoRule koRule = new KoRule();

  public int blackPlacedStones = 0;
  public int whitePlacedStones = 0;
  public int blackPassStones = 0;
  public int whitePassStones = 0;

  public int getBlackPlacedStones() {
    return blackPlacedStones;
  }

  public int getWhitePlacedStones() {
    return whitePlacedStones;
  }

  public int getBlackPassStones() {
    return blackPassStones;
  }

  public int getWhitePassStones() {
    return whitePassStones;
  }

  private final List<GameObserver> observers = new ArrayList<>();

  public Game(Board board) {
    this.board = board;
    this.state = new WaitingForPlayers();
  }

  public void addPlayer(String playerId) {
    state.addPlayer(this, playerId);
  }

  public void makeMove(Move move) {
    state.makeMove(this, move);
  }

  /* package-private - for state */
  void setState(GameState state) {
    this.state = state;
  }

  void switchTurn() {
    currentTurn = currentTurn.opposite();
  }

  public Color getCurrentTurn() {
    return currentTurn;
  }

  public Board getBoard() {
    return board;
  }

  public KoRule getKoRule() {
    return koRule;
  }

  /* ===== Observer ===== */

  public void addObserver(GameObserver observer) {
    observers.add(observer);
  }

  void notifyObservers(GameEvent event) {
    observers.forEach(o -> o.onGameEvent(event));
  }
}
