package edu.university.go.scoring;

import edu.university.go.board.Color;
import edu.university.go.board.Point;
import java.util.HashSet;
import java.util.Set;

class Territory {
  private final Set<Point> points = new HashSet<>();
  private final Set<Color> borders = new HashSet<>();

  public void addPoint(Point point) {
    points.add(point);
  }

  public void addBorder(Color color) {
    if (color != Color.EMPTY) {
      borders.add(color);
    }
  }

  public Set<Point> getPoints() {
    return points;
  }

  public Set<Color> getBorders() {
    return borders;
  }
}
