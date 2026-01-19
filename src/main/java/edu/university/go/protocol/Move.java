package edu.university.go.protocol;

public class Move {

  private final int x;
  private final int y;

  public Move(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }
}
