package edu.university.go.server;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.game.Game;
import edu.university.go.game.Move;
import java.util.ArrayList;
import java.util.List;
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

    assertTrue(c1.hasReceivedMessageContaining("MOVE_PLAYED"), 
               "Client 1 should have received MOVE_PLAYED event");
    assertTrue(c2.hasReceivedMessageContaining("MOVE_PLAYED"), 
               "Client 2 should have received MOVE_PLAYED event");
  }

  @Test
  void invalidMoveSendsErrorToPlayer() {
    Game game = new Game(new Board(9));
    GameSession session = new GameSession(game);

    FakeClient c1 = new FakeClient();
    session.addPlayer("p1", c1);

    session.handleMove(new Move(Color.BLACK, 4, 4, "p1"));

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertTrue(c1.receivedErrorMessage, "Should have received an ERROR message");
  }

  static class FakeClient extends ClientHandler {

    String lastMessage;
    boolean receivedErrorMessage = false;
    
    List<String> allMessages = new ArrayList<>();

    FakeClient() {
      super(null, null);
    }

    @Override
    void send(String msg) {
      lastMessage = msg;
      allMessages.add(msg);
      if (msg.startsWith("ERROR")) {
        receivedErrorMessage = true;
      }
    }

    boolean hasReceivedMessageContaining(String text) {
      return allMessages.stream().anyMatch(msg -> msg.contains(text));
    }
  }
}