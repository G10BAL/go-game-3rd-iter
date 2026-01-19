package edu.university.go.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameStateTest {

  private Game game;

  @BeforeEach
  void setup() {
    Board board = new Board(9);
    game = new Game(board);
  }

  @Test
  void cannotMakeMoveBeforeTwoPlayersJoin() {
    game.addPlayer("p1");

    Move move = new Move(Color.BLACK, 4, 4, "p1");

    assertThrows(IllegalStateException.class, () -> game.makeMove(move));
  }

  @Test
  void gameStartsWhenTwoPlayersJoin() {
    game.addPlayer("p1");
    game.addPlayer("p2");

    Move move = new Move(Color.BLACK, 4, 4, "p1");

    assertDoesNotThrow(() -> game.makeMove(move));
  }

  @Test
  void blackAlwaysStarts() {
    game.addPlayer("p1");
    game.addPlayer("p2");

    Move blackMove = new Move(Color.BLACK, 4, 4, "p1");

    game.makeMove(blackMove);

    assertEquals(Color.WHITE, game.getCurrentTurn());
  }

  @Test
  void cannotPlayTwiceInARow() {
    game.addPlayer("p1");
    game.addPlayer("p2");

    game.makeMove(new Move(Color.BLACK, 4, 4, "p1"));

    Move illegalMove = new Move(Color.BLACK, 5, 5, "p1");

    assertThrows(IllegalStateException.class, () -> game.makeMove(illegalMove));
  }

  @Test
  void unknownPlayerCannotMakeMove() {
    game.addPlayer("p1");
    game.addPlayer("p2");

    Move move = new Move(Color.BLACK, 4, 4, "intruder");

    assertThrows(IllegalArgumentException.class, () -> game.makeMove(move));
  }

  @Test
  void correctTurnSequenceWorks() {
    game.addPlayer("p1");
    game.addPlayer("p2");

    game.makeMove(new Move(Color.BLACK, 4, 4, "p1"));
    game.makeMove(new Move(Color.WHITE, 5, 5, "p2"));

    assertEquals(Color.BLACK, game.getCurrentTurn());
  }
}
