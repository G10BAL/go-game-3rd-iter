package edu.university.go.server;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.game.Game;
import edu.university.go.game.Move;
import org.junit.jupiter.api.Test;

class GameSessionTest {

  @Test
  void validMoveIsBroadcasted() {
    Game game = new Game(new Board(9));
    GameSession session = new GameSession(game);

    FakeClient c1 = new FakeClient();
    FakeClient c2 = new FakeClient();

    session.addPlayer("p1", c1);
    session.addPlayer("p2", c2);

    session.handleMove(new Move(Color.BLACK, 4, 4, "p1"));

    assertTrue(c1.lastMessage.contains("MOVE_PLAYED"));
    assertTrue(c2.lastMessage.contains("MOVE_PLAYED"));
  }

  @Test
  void invalidMoveSendsErrorToPlayer() {
    Game game = new Game(new Board(9));
    GameSession session = new GameSession(game);

    FakeClient c1 = new FakeClient();
    session.addPlayer("p1", c1);

    session.handleMove(new Move(Color.BLACK, 4, 4, "p1"));

    // Wait a bit for async operations to complete
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Check that an error message was sent (either first or among messages)
    assertTrue(c1.receivedErrorMessage, "Should have received an ERROR message");
  }

  static class FakeClient extends ClientHandler {

    String lastMessage;
    boolean receivedErrorMessage = false;

    FakeClient() {
      super(null, null);
    }

    @Override
    void send(String msg) {
      lastMessage = msg;
      if (msg.startsWith("ERROR")) {
        receivedErrorMessage = true;
      }
    }
  }
}
