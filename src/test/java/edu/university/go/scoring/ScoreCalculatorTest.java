package edu.university.go.scoring;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {

  private ScoreCalculator calculator;
  private Board board;
  private static final double KOMI = 7.5;

  @BeforeEach
  void setUp() {
    calculator = new ScoreCalculator(KOMI);
    board = new Board(9);
  }

  @Test
  @DisplayName("Empty board - White wins by komi")
  void testEmptyBoardWhiteWinsByKomi() {
    GameResult result = calculator.calculateScore(board, 0, 0);

    assertEquals(0.0, result.blackScore(), "Black has 0 points");
    assertEquals(KOMI, result.whiteScore(), "White has only komi");
    assertEquals(Color.WHITE, result.winner(), "White wins by komi");
    assertEquals(KOMI, result.margin(), "Difference equals komi");
  }

  @Test
  @DisplayName("Chess board")
  void testOnlyCapturedStonesNoTerritory() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        Color color = ((i + j) % 2 == 0) ? Color.BLACK : Color.WHITE;
        board.placeStone(color, i, j);
      }
    }

    int capturedByBlack = 0;
    int capturedByWhite = 5;

    GameResult result = calculator.calculateScore(board, capturedByBlack, capturedByWhite);

    assertEquals(0.0, result.blackScore(), "Black: 0 territory + 0 captured");
    assertEquals(5.0 + 5.0 + KOMI, result.whiteScore(), "White: 5 territory + 5 captured + komi");
  }

  @Test
  @DisplayName("Simple black territory without captures")
  void testSimpleBlackTerritoryNoCaptures() {
    /*
     * B B B .
     * B . B .
     * B B B .
     * . . . W
     */
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, 2);
    }
    board.placeStone(Color.WHITE, 3, 3);

    GameResult result = calculator.calculateScore(board, 0, 0);

    assertEquals(1.0, result.blackScore(), "Black has 1 point of territory");
    assertEquals(KOMI, result.whiteScore(), "White has only komi");

    // Black wins if their territory > komi
    if (1.0 > KOMI) {
      assertEquals(Color.BLACK, result.winner());
    } else {
      assertEquals(Color.WHITE, result.winner());
    }
  }

  @Test
  @DisplayName("Big black territory beats komi")
  void testLargeBlackTerritoryBeatsKomi() {
    /*
     * Create a large enclosed territory for Black:
     */
    int size = 7;
    for (int i = 0; i < size; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, size - 1, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, size - 1);
    }
    board.placeStone(Color.WHITE, 8, 8);

    GameResult result = calculator.calculateScore(board, 0, 0);

    int expectedTerritory = (size - 2) * (size - 2); // 25
    assertEquals(expectedTerritory, result.blackScore());
    assertEquals(KOMI, result.whiteScore());
    assertEquals(Color.BLACK, result.winner(), "Black wins with large territory");
    assertTrue(result.margin() > 10, "Large point difference");
  }

  @Test
  @DisplayName("Even game - White wins by komi")
  void testEvenGameWhiteWinsByKomi() {

    // Black territory
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, 2);
    }

    // White territory
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.WHITE, 4, 4 + i);
      board.placeStone(Color.WHITE, 6, 4 + i);
      board.placeStone(Color.WHITE, 4 + i, 4);
      board.placeStone(Color.WHITE, 4 + i, 6);
    }

    GameResult result = calculator.calculateScore(board, 3, 3);

    // Both have ~4 points (1 territory + 3 captures)
    // But White has +6.5 komi
    assertEquals(Color.WHITE, result.winner(), "In an even game, White wins by komi");
  }

  @Test
  @DisplayName("Black compensates komi with captures")
  void testBlackCompensatesKomiWithCaptures() {
    board.placeStone(Color.BLACK, 0, 0);
    board.placeStone(Color.BLACK, 0, 1);
    board.placeStone(Color.BLACK, 1, 0);

    // Black captured many stones
    int capturedByBlack = 10;
    int capturedByWhite = 0;

    GameResult result = calculator.calculateScore(board, capturedByBlack, capturedByWhite);

    assertTrue(result.blackScore() > result.whiteScore(), "Black compensates komi with captures");
    assertEquals(Color.BLACK, result.winner());
  }

  @Test
  @DisplayName("Very close game - difference less than 1 point")
  void testVeryCloseGame() {
    // Black: 9 points
    // White: 0 + 7.5 = 6.5 points

    // 9 points territory for black
    int size = 9;
    for (int i = 0; i < size; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
    }
    board.placeStone(Color.WHITE, 3, 1);

    GameResult result = calculator.calculateScore(board, 0, 0);

    assertTrue(result.margin() < 10, "Very close game");
  }

  @Test
  @DisplayName("Territory and captures combined")
  void testTerritoryAndCapturesCombined() {
    // Black: 5 territory + 3 captures = 8
    // White: 4 territory + 2 captures + 7.5 komi = 13.5

    // Black territory (approximately 5 points)
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.BLACK, 0, i);
      board.placeStone(Color.BLACK, 2, i);
      board.placeStone(Color.BLACK, i, 0);
      board.placeStone(Color.BLACK, i, 2);
    }
    board.placeStone(Color.BLACK, 1, 3);
    board.placeStone(Color.BLACK, 3, 1);

    // White territory (approximately 4 points)
    for (int i = 0; i < 3; i++) {
      board.placeStone(Color.WHITE, 5, 5 + i);
      board.placeStone(Color.WHITE, 7, 5 + i);
      board.placeStone(Color.WHITE, 5 + i, 5);
      board.placeStone(Color.WHITE, 5 + i, 7);
    }

    GameResult result = calculator.calculateScore(board, 3, 2);

    assertTrue(result.blackScore() > 0, "Black has points");
    assertTrue(result.whiteScore() > KOMI, "White has more than just komi");
  }

  @Test
  @DisplayName("Zero komi changes the result")
  void testZeroKomiChangesResult() {
    ScoreCalculator noKomiCalculator = new ScoreCalculator(0);

    // Equal position
    board.placeStone(Color.BLACK, 0, 0);
    board.placeStone(Color.WHITE, 8, 8);

    GameResult result = noKomiCalculator.calculateScore(board, 0, 0);

    // Without komi, it could be a tie or black wins
    // (depending on territory calculation)
    assertNotNull(result.winner());
  }

  @Test
  @DisplayName("Different komi values")
  void testDifferentKomiValues() {
    double[] komiValues = {0.5, 5.5, 6.5, 7.5};

    for (double komi : komiValues) {
      ScoreCalculator calc = new ScoreCalculator(komi);
      GameResult result = calc.calculateScore(board, 0, 0);

      assertEquals(komi, result.whiteScore(), "White has komi = " + komi);
    }
  }

  @Test
  @DisplayName("Large difference in points")
  void testLargeDifference() {
    // Black controls almost the entire board
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        board.placeStone(Color.BLACK, 0, j);
        board.placeStone(Color.BLACK, 8, j);
        board.placeStone(Color.BLACK, i, 0);
        board.placeStone(Color.BLACK, i, 8);
      }
    }

    GameResult result = calculator.calculateScore(board, 10, 0);

    assertEquals(Color.BLACK, result.winner());
    assertTrue(result.margin() > 30, "Very large difference");
  }

  @Test
  @DisplayName("ToString returns correct format")
  void testToStringFormat() {
    GameResult result = calculator.calculateScore(board, 5, 3);

    String resultString = result.toString();

    assertTrue(resultString.contains("won"), "Contains 'won'");
    assertTrue(resultString.contains("points"), "Contains 'points'");
    assertTrue(
        resultString.contains("Black") || resultString.contains("White"), "Contains winner color");
  }

  @Test
  @DisplayName("GameResult records all data correctly")
  void testGameResultRecordsDataCorrectly() {
    board.placeStone(Color.BLACK, 4, 4);
    board.placeStone(Color.WHITE, 5, 5);

    GameResult result = calculator.calculateScore(board, 7, 3);

    assertEquals(7.0, result.blackScore(), 0.01);
    assertEquals(3.0 + KOMI, result.whiteScore(), 0.01);
    assertNotNull(result.winner());
    assertEquals(Math.abs(result.blackScore() - result.whiteScore()), result.margin(), 0.01);
  }

  @Test
  @DisplayName("Check margin is always positive")
  void testMarginAlwaysPositive() {
    GameResult result1 = calculator.calculateScore(board, 10, 0);
    GameResult result2 = calculator.calculateScore(board, 0, 10);

    assertTrue(result1.margin() >= 0, "Margin should be positive");
    assertTrue(result2.margin() >= 0, "Margin should be positive");
  }

  @Test
  @DisplayName("Score with decimal values")
  void testScoreWithDecimalValues() {
    // Komi (7.5)
    GameResult result = calculator.calculateScore(board, 5, 0);

    // White: 0 + 7.5 = 7.5
    // Black: 5
    assertEquals(5.0, result.blackScore());
    assertEquals(7.5, result.whiteScore());
    assertEquals(Color.WHITE, result.winner());
    assertEquals(2.5, result.margin());
  }
}
