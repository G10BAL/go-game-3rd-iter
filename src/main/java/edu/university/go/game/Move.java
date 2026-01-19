package edu.university.go.game;

import edu.university.go.board.Color;

public record Move(Color color, int x, int y, String playerId, MoveType type) {
  public Move(Color color, int x, int y, String playerId) {
    this(color, x, y, playerId, MoveType.PLACE_STONE);
  }

  public static Move pass(Color color, String playerId) {
    return new Move(color, -1, -1, playerId, MoveType.PASS);
  }

  public static Move resign(Color color, String playerId) {
    return new Move(color, -1, -1, playerId, MoveType.RESIGN);
  }

  public boolean isPass() {
    return type == MoveType.PASS;
  }

  public boolean isResign() {
    return type == MoveType.RESIGN;
  }
}

enum MoveType {
  PLACE_STONE,
  PASS,
  RESIGN
}
