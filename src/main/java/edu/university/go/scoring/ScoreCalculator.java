package edu.university.go.scoring;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import java.util.Map;

public class ScoreCalculator {

  private final TerritoryCounter territoryCounter;
  private final double komi; // Komi

  public ScoreCalculator(double komi) {
    this.territoryCounter = new TerritoryCounter();
    this.komi = komi;
  }

  // Calculate final scores and determine the winner
  public GameResult calculateScore(Board board, int capturedByBlack, int capturedByWhite) {
    // Calculate territory
    Map<Color, Integer> territory = territoryCounter.calculateTerritory(board);

    double blackScore = territory.get(Color.BLACK) + capturedByBlack;
    double whiteScore = territory.get(Color.WHITE) + capturedByWhite + komi;

    Color winner = blackScore > whiteScore ? Color.BLACK : Color.WHITE;
    double margin = Math.abs(blackScore - whiteScore);

    return new GameResult(blackScore, whiteScore, winner, margin);
  }
}
