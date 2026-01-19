package edu.university.go.scoring;

import edu.university.go.board.Color;

public record GameResult(double blackScore, double whiteScore, Color winner, double margin) {
  @Override
  public String toString() {
    return String.format(
        "%s won with the difference %.1f points (Black: %.1f, White: %.1f)",
        winner, margin, blackScore, whiteScore);
  }
}
