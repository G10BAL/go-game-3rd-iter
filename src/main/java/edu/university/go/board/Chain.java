package edu.university.go.board;

import java.util.HashSet;
import java.util.Set;

public class Chain {
  private final Color color;
  private final Set<Point> stones = new HashSet<>();

  public Chain(Color color) {
    this.color = color;
  }

  public void add(Point p) {
    stones.add(p);
  }

  public Set<Point> getStones() {
    return stones;
  }

  public Color getColor() {
    return color;
  }

  public Set<Point> getLiberties(Board board) {
    Set<Point> liberties = new HashSet<>();
    for (Point stone : stones) {
      for (Point neighbor : board.neighbors(stone)) {
        if (board.get(neighbor.x, neighbor.y) == Color.EMPTY) {
          liberties.add(neighbor);
        }
      }
    }
    return liberties;
  }

  public boolean isCaptured(Board board) {
    return getLiberties(board).isEmpty();
  }

  public int size() {
    return stones.size();
  }
}
