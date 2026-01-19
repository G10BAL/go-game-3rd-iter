package edu.university.go.server;

import edu.university.go.board.Board;
import edu.university.go.game.Game;

public class GameFactory {

  public static Game createGame(int boardSize) {
    return new Game(new Board(boardSize));
  }
}
