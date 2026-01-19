package edu.university.go.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameObserverTest {

  @Test
  void observerReceivesGameStartedEvent() {
    Board board = new Board(9);
    Game game = new Game(board);

    List<GameEvent> events = new ArrayList<>();

    game.addObserver(events::add);

    game.addPlayer("p1");
    game.addPlayer("p2");

    assertTrue(events.contains(GameEvent.GAME_STARTED));
  }

  @Test
  void observerReceivesMovePlayedEvent() {
    Board board = new Board(9);
    Game game = new Game(board);

    List<GameEvent> events = new ArrayList<>();
    game.addObserver(events::add);

    game.addPlayer("p1");
    game.addPlayer("p2");

    game.makeMove(new Move(Color.BLACK, 4, 4, "p1"));

    assertTrue(events.contains(GameEvent.MOVE_PLAYED));
  }
}
