package edu.university.go.scoring;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.board.Point;
import java.util.*;

public class TerritoryCounter {

  // Calculate territory for each player
  public Map<Color, Integer> calculateTerritory(Board board) {
    Map<Color, Integer> territory = new HashMap<>();
    territory.put(Color.BLACK, 0);
    territory.put(Color.WHITE, 0);

    Set<Point> visited = new HashSet<>();

    // Iterate over all points on the board
    for (int x = 0; x < board.getSize(); x++) {
      for (int y = 0; y < board.getSize(); y++) {
        Point p = new Point(x, y);

        if (board.get(x, y) == Color.EMPTY && !visited.contains(p)) {
          Territory t = floodFill(board, p, visited);
          Color owner = determineOwner(t);

          if (owner != Color.EMPTY) {
            territory.put(owner, territory.get(owner) + t.getPoints().size());
          }
        }
      }
    }

    return territory;
  }

  // Algorithm to find all connected empty points (territory)
  private Territory floodFill(Board board, Point start, Set<Point> globalVisited) {
    Territory territory = new Territory();
    Queue<Point> queue = new LinkedList<>();
    Set<Point> localVisited = new HashSet<>();

    queue.add(start);
    localVisited.add(start);

    while (!queue.isEmpty()) {
      Point current = queue.poll();
      territory.addPoint(current);
      globalVisited.add(current);

      for (Point neighbor : board.neighbors(current)) {
        if (localVisited.contains(neighbor)) {
          continue;
        }

        Color neighborColor = board.get(neighbor.x, neighbor.y);

        if (neighborColor == Color.EMPTY) {
          // Empty point add to queue
          queue.add(neighbor);
          localVisited.add(neighbor);
        } else {
          // Taken point, add to borders
          territory.addBorder(neighborColor);
        }
      }
    }

    return territory;
  }

  // Determine the owner of the territory based on bordering colors
  private Color determineOwner(Territory territory) {
    Set<Color> borders = territory.getBorders();

    // If only one color borders the territory
    if (borders.size() == 1) {
      Color owner = borders.iterator().next();
      if (owner != Color.EMPTY) {
        return owner;
      }
    }

    // Otherwise - neutral territory
    return Color.EMPTY;
  }
}
