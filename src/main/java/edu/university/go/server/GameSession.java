package edu.university.go.server;

import edu.university.go.board.Color;
import edu.university.go.game.*;
import edu.university.go.scoring.GameResult;
import edu.university.go.scoring.ScoreCalculator;
import java.util.HashMap;
import java.util.Map;

class GameSession implements GameObserver {

  private final Game game;
  private final ScoreCalculator scoreCalculator;
  private final Map<String, ClientHandler> players = new HashMap<>();
  private final Map<String, Color> playerColors = new HashMap<>();

  private int capturedByBlack = 0;
  private int capturedByWhite = 0;
  private edu.university.go.board.Board previousBoard = null;

  GameSession(Game game) {
    this.game = game;
    this.scoreCalculator = new ScoreCalculator(7.5); // Standard komi
    this.game.addObserver(this);
  }

  void addPlayer(String playerId, ClientHandler handler) {
    players.put(playerId, handler);

    int size = game.getBoard().getSize();
    System.out.println("[GameSession] Sending BOARDSIZE " + size + " to player " + playerId);
    handler.send("BOARDSIZE " + size);

    Color color = players.size() == 1 ? Color.BLACK : Color.WHITE;
    String assigned = color == Color.BLACK ? "BLACK" : "WHITE";
    playerColors.put(playerId, color);
    System.out.println("[GameSession] Sending COLOR " + assigned + " to player " + playerId);
    handler.send("COLOR " + assigned);

    try {
      game.addPlayer(playerId);
    } catch (Exception e) {
      handler.send("ERROR: " + e.getMessage());
      players.remove(playerId);
      playerColors.remove(playerId);
      return;
    }
  }

  void handleMove(Move move) {
    try {
      int blackStonesBefore = countStones(Color.BLACK);
      int whiteStonesBefore = countStones(Color.WHITE);

      game.makeMove(move);

      int blackStonesAfter = countStones(Color.BLACK);
      int whiteStonesAfter = countStones(Color.WHITE);

      if (move.color() == Color.BLACK && whiteStonesBefore > whiteStonesAfter) {
        capturedByBlack += (whiteStonesBefore - whiteStonesAfter);
      }
      if (move.color() == Color.WHITE && blackStonesBefore > blackStonesAfter) {
        capturedByWhite += (blackStonesBefore - blackStonesAfter);
      }

      System.out.println(
          "[GameSession] After move - BLACK captured: "
              + capturedByBlack
              + ", WHITE captured: "
              + capturedByWhite);

    } catch (Exception e) {
      players.get(move.playerId()).send("ERROR: " + e.getMessage());
    }
  }

  private int countStones(Color color) {
    int count = 0;
    edu.university.go.board.Board board = game.getBoard();
    for (int x = 0; x < board.getSize(); x++) {
      for (int y = 0; y < board.getSize(); y++) {
        if (board.get(x, y) == color) {
          count++;
        }
      }
    }
    return count;
  }

  void handlePass(String playerId) {
    try {
      Color playerColor = playerColors.get(playerId);
      if (playerColor == null) {
        throw new IllegalStateException("Player color not found");
      }
      Move pass = Move.pass(playerColor, playerId);
      game.makeMove(pass);
    } catch (Exception e) {
      ClientHandler handler = players.get(playerId);
      if (handler != null) {
        handler.send("ERROR: " + e.getMessage());
        new Thread(
                () -> {
                  try {
                    Thread.sleep(10);
                    sendBoard(playerId);
                  } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                  }
                })
            .start();
      }
    }
  }

  void handleResign(String playerId) {
    try {
      Color playerColor = playerColors.get(playerId);
      if (playerColor == null) {
        throw new IllegalStateException("Player color not found");
      }
      Move resign = Move.resign(playerColor, playerId);
      game.makeMove(resign);
    } catch (Exception e) {
      ClientHandler handler = players.get(playerId);
      if (handler != null) {
        handler.send("ERROR: " + e.getMessage());
      }
    }
  }

  @Override
  public void onGameEvent(GameEvent event) {
    broadcast("EVENT " + event);

    if (event == GameEvent.GAME_STARTED || event == GameEvent.MOVE_PLAYED) {
      // Counting captured stones
      int countBlack = countStones(Color.BLACK);
      int countWhite = countStones(Color.WHITE);
      capturedByBlack = (game.getWhitePlacedStones() - countWhite) + game.getWhitePassStones();
      capturedByWhite = (game.getBlackPlacedStones() - countBlack) + game.getBlackPassStones();

      System.out.println(
          "[GameSession] Broadcasting game state - CAPTURED: "
              + capturedByBlack
              + " "
              + capturedByWhite
              + ", TURN: "
              + game.getCurrentTurn());

      // Sending board
      for (String pid : players.keySet()) {
        sendBoard(pid);
      }

      // Then state
      broadcast("CAPTURED " + capturedByBlack + " " + capturedByWhite);
      broadcast("TURN " + game.getCurrentTurn());

      broadcast("EVENT " + event);
    } else if (event == GameEvent.GAME_ENDED) {

      int countBlack = countStones(Color.BLACK);
      int countWhite = countStones(Color.WHITE);
      capturedByBlack = (game.getWhitePlacedStones() - countWhite) + game.getWhitePassStones();
      capturedByWhite = (game.getBlackPlacedStones() - countBlack) + game.getBlackPassStones();

      System.out.println("[GameSession] Game ended");

      GameResult result =
          scoreCalculator.calculateScore(game.getBoard(), capturedByBlack, capturedByWhite);
      System.out.println("[GameSession] Final result: " + result);

      for (String pid : players.keySet()) {
        sendBoard(pid);
      }

      broadcast("CAPTURED " + capturedByBlack + " " + capturedByWhite);
      broadcast("TURN " + game.getCurrentTurn());

      String scoreMsg =
          String.format(
              "RESULT %s %.1f %.1f %.1f",
              result.winner(), result.blackScore(), result.whiteScore(), result.margin());
      System.out.println("[GameSession] Sending score: " + scoreMsg);
      broadcast(scoreMsg);

      broadcast("EVENT " + event);
    }
  }

  private void broadcast(String msg) {
    players.values().forEach(p -> p.send(msg));
  }

  void sendBoard(String playerId) {
    if (game == null) return;
    edu.university.go.board.Board b = game.getBoard();
    int size = b.getSize();
    ClientHandler h = players.get(playerId);
    if (h == null) return;

    StringBuilder header = new StringBuilder("   ");
    for (int x = 0; x < size; x++) {
      header.append(String.format(" %2d", x));
    }
    h.send(header.toString());

    for (int y = 0; y < size; y++) {
      StringBuilder row = new StringBuilder();
      row.append(String.format("%2d ", y));
      for (int x = 0; x < size; x++) {
        edu.university.go.board.Color c = b.get(x, y);
        char ch = '.';
        if (c == edu.university.go.board.Color.BLACK) ch = 'B';
        else if (c == edu.university.go.board.Color.WHITE) ch = 'W';
        row.append(String.format("  %c", ch));
      }
      h.send(row.toString());
    }

    h.send("");
  }

  GameSession() {
    this.game = null;
    this.scoreCalculator = new ScoreCalculator(7.5);
  }
}
