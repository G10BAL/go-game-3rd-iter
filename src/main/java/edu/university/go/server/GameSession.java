package edu.university.go.server;

import edu.university.go.board.Color;
import edu.university.go.db.GameEntity;
import edu.university.go.db.GameService;
import edu.university.go.game.*;
import edu.university.go.scoring.GameResult;
import edu.university.go.scoring.ScoreCalculator;

import java.util.HashMap;
import java.util.Map;

/*
 * Game session class that manages a game instance, players, moves
 */
class GameSession implements GameObserver {

  private final Game game;
  private final ScoreCalculator scoreCalculator;
  private final Map<String, ClientHandler> players = new HashMap<>();
  private final Map<String, Color> playerColors = new HashMap<>();
  private final Map<String, PlayerType> playerTypes = new HashMap<>();

  private int capturedByBlack = 0;
  private int capturedByWhite = 0;
  
  private final GameService gameService;
  private GameEntity gameEntity;
  private int turnNumber = 0;
  
  private final boolean isRestored;
  
  private BotPlayer botPlayer;

  /**
   * A constructor for new games.
   */
  GameSession(Game game, GameService gameService, int boardSize) {
    this.game = game;
    this.scoreCalculator = new ScoreCalculator(7.5);
    this.game.addObserver(this);
    this.gameService = gameService;
    this.isRestored = false;
    
    if (gameService != null) {
      this.gameEntity = gameService.createGame(boardSize, null, null);
      System.out.println("[GameSession] Game entity created: ID=" + gameEntity.getId());
    } else {
      System.out.println("[GameSession] Running without database");
    }
  }
  
  /**
   * A constructor for restored games.
   * Takes additional parameters for turn number and captured stones.
   */
  GameSession(Game game, GameEntity gameEntity, GameService gameService, 
              int turnNumber, int capturedByBlack, int capturedByWhite) {
    this.game = game;
    this.scoreCalculator = new ScoreCalculator(7.5);
    this.game.addObserver(this);
    this.gameService = gameService;
    this.gameEntity = gameEntity;
    this.turnNumber = turnNumber;
    this.isRestored = true;
    
    this.capturedByBlack = capturedByBlack;
    this.capturedByWhite = capturedByWhite;
    
    System.out.println("[GameSession] Restored game session: ID=" + gameEntity.getId() + 
                      ", moves=" + turnNumber);
    System.out.println("[GameSession] Restored captured stones - Black: " + capturedByBlack + 
                      ", White: " + capturedByWhite);
  }
  
  /**
   * Uses LoadedGame to create a restored GameSession
   */
  public static GameSession createRestoredSession(
      GameService.LoadedGame loadedGame,
      GameEntity gameEntity, 
      GameService gameService) {
    
    int moveCount = gameService.getMoveCount(gameEntity.getId());
    
    return new GameSession(
        loadedGame.game,
        gameEntity,
        gameService,
        moveCount,
        loadedGame.capturedByBlack,
        loadedGame.capturedByWhite
    );
  }
  
  GameSession(Game game) {
    this(game, null, 19);
  }

  void addPlayer(String playerId, ClientHandler handler) {
    addPlayer(playerId, handler, PlayerType.HUMAN);
  }

  void addPlayer(String playerId, ClientHandler handler, PlayerType playerType) {
    players.put(playerId, handler);
    playerTypes.put(playerId, playerType);

    int size = game.getBoard().getSize();
    
    if (handler != null) {
      System.out.println("[GameSession] Sending BOARDSIZE " + size + " to player " + playerId);
      handler.send("BOARDSIZE " + size);
    }

    Color color = players.size() == 1 ? Color.BLACK : Color.WHITE;
    String assigned = color == Color.BLACK ? "BLACK" : "WHITE";
    playerColors.put(playerId, color);
    
    if (gameEntity != null && gameService != null && !isRestored) {
        if (color == Color.BLACK) {
            gameEntity.setBlackType(playerType);
            System.out.println("[GameSession] Set BLACK player type to: " + playerType);
        } else {
            gameEntity.setWhiteType(playerType);
            System.out.println("[GameSession] Set WHITE player type to: " + playerType);
        }
        
        gameService.updatePlayerTypes(gameEntity.getId(), 
                                      gameEntity.getBlackType(), 
                                      gameEntity.getWhiteType());
        System.out.println("[GameSession] Updated player types in DB: Black=" + 
                          gameEntity.getBlackType() + ", White=" + gameEntity.getWhiteType());
    }

    if (handler != null) {
      System.out.println("[GameSession] Sending COLOR " + assigned + " to player " + playerId);
      handler.send("COLOR " + assigned);
    }

    try {
      game.addPlayer(playerId);
      System.out.println("[GameSession] Added player to game: " + playerId + " (" + color + ", " + playerType + ")");
      
      if (playerType == PlayerType.BOT && botPlayer == null) {
        botPlayer = new BotPlayer(game, color);
        System.out.println("[GameSession] Bot initialized for " + color);
      }
      
      if (isRestored && handler != null) {
        System.out.println("[GameSession] Sending restored state to " + playerId);
        handler.send("CAPTURED " + capturedByBlack + " " + capturedByWhite);
        handler.send("TURN " + game.getCurrentTurn());
        sendBoard(playerId);
        
        if ("FINISHED".equals(gameEntity.getStatus())) {
          String scoreMsg = String.format(
              "RESULT %s %.1f %.1f %.1f",
              gameEntity.getWinner(),
              gameEntity.getBlackScore(),
              gameEntity.getWhiteScore(),
              Math.abs(gameEntity.getBlackScore() - gameEntity.getWhiteScore())
          );
          handler.send(scoreMsg);
          handler.send("EVENT GAME_ENDED");
        }
      }
      
    } catch (Exception e) {
      System.err.println("[GameSession] Error adding player: " + e.getMessage());
      e.printStackTrace();
      if (handler != null) {
        handler.send("ERROR: " + e.getMessage());
      }
      players.remove(playerId);
      playerColors.remove(playerId);
      playerTypes.remove(playerId);
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

      saveMoveToDatabase(move, MoveType.PLACE_STONE);

    } catch (Exception e) {
      ClientHandler handler = players.get(move.playerId());
      if (handler != null) {
        handler.send("ERROR: " + e.getMessage());
      }
    }
    broadcast("CAPTURED " + capturedByBlack + " " + capturedByWhite);
  }
  
  private void saveMoveToDatabase(Move move, MoveType moveType) {
    if (gameEntity != null && gameService != null) {
      try {
        turnNumber++;
        gameService.saveMove(gameEntity, turnNumber, move, moveType);
        System.out.println("[GameSession] Move #" + turnNumber + " saved to database");
      } catch (Exception e) {
        System.err.println("[GameSession] Failed to save move: " + e.getMessage());
        e.printStackTrace();
      }
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
      
      saveMoveToDatabase(pass, MoveType.PASS);
      
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
    broadcast("CAPTURED " + capturedByBlack + " " + capturedByWhite);
  }

  void handleResign(String playerId) {
    try {
      Color playerColor = playerColors.get(playerId);
      if (playerColor == null) {
        throw new IllegalStateException("Player color not found");
      }
      Move resign = Move.resign(playerColor, playerId);
      game.makeMove(resign);
      
      saveMoveToDatabase(resign, MoveType.RESIGN);
      
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
      if (!isRestored && event == GameEvent.GAME_STARTED) {
        int countBlack = countStones(Color.BLACK);
        int countWhite = countStones(Color.WHITE);
        capturedByBlack = (game.getWhitePlacedStones() - countWhite) + game.getWhitePassStones();
        capturedByWhite = (game.getBlackPlacedStones() - countBlack) + game.getBlackPassStones();
      }

      // Send CAPTURED and TURN before the board so the client UI updates in correct order
      broadcast("CAPTURED " + capturedByBlack + " " + capturedByWhite);
      broadcast("TURN " + game.getCurrentTurn());

      // Now send the board state
      for (String pid : players.keySet()) {
        sendBoard(pid);
      }

      broadcast("EVENT " + event);
      
      if (event == GameEvent.MOVE_PLAYED) {
        checkAndMakeBotMove();
      }
      
    } else if (event == GameEvent.GAME_ENDED) {
      // Only recalculate captured stones for non-restored games
      // For restored games, we already have the correct values from the incremental tracking during replay
      if (!isRestored) {
        int countBlack = countStones(Color.BLACK);
        int countWhite = countStones(Color.WHITE);
        capturedByBlack = (game.getWhitePlacedStones() - countWhite) + game.getWhitePassStones();
        capturedByWhite = (game.getBlackPlacedStones() - countBlack) + game.getBlackPassStones();
      }

      System.out.println("[GameSession] Game ended");
      System.out.println("[GameSession] Final captured stones - Black: " + capturedByBlack + 
                        ", White: " + capturedByWhite);

      GameResult result =
          scoreCalculator.calculateScore(game.getBoard(), capturedByBlack, capturedByWhite);
      System.out.println("[GameSession] Final result: " + result);

      // Send game state info before the board
      broadcast("CAPTURED " + capturedByBlack + " " + capturedByWhite);
      broadcast("TURN " + game.getCurrentTurn());
      
      String scoreMsg =
          String.format(
              "RESULT %s %.1f %.1f %.1f",
              result.winner(), result.blackScore(), result.whiteScore(), result.margin());
      System.out.println("[GameSession] Sending score: " + scoreMsg);
      broadcast(scoreMsg);

      // Now send the final board state
      for (String pid : players.keySet()) {
        sendBoard(pid);
      }

      broadcast("EVENT " + event);
      
      if (gameEntity != null && gameService != null) {
        try {
          gameService.updateGameStatus(
              gameEntity.getId(),
              "FINISHED",
              result.winner().toString(),
              result.blackScore(),
              result.whiteScore(),
              capturedByBlack,
              capturedByWhite
          );
          System.out.println("[GameSession] Game status updated in database");
        } catch (Exception e) {
          System.err.println("[GameSession] Failed to update game status: " + e.getMessage());
        }
      }
    }
  }
  
  private void checkAndMakeBotMove() {
    if (botPlayer == null) return;
    
    Color currentTurn = game.getCurrentTurn();
    
    for (Map.Entry<String, Color> entry : playerColors.entrySet()) {
      if (entry.getValue() == currentTurn) {
        String playerId = entry.getKey();
        PlayerType playerType = playerTypes.get(playerId);
        
        if (playerType == PlayerType.BOT) {
          new Thread(() -> {
            try {
              Thread.sleep(500);
              Move botMove = botPlayer.getNextMove();
              System.out.println("[GameSession] Bot making move: " + botMove);
              
              if (botMove.type() == MoveType.PLACE_STONE) {
                handleMove(botMove);
              } else if (botMove.type() == MoveType.PASS) {
                handlePass(playerId);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (Exception e) {
              System.err.println("[GameSession] Bot move error: " + e.getMessage());
            }
          }).start();
        }
        break;
      }
    }
  }

  private void broadcast(String msg) {
    players.values().forEach(p -> {
      if (p != null) {
        p.send(msg);
      }
    });
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
  
  public GameEntity getGameEntity() {
    return gameEntity;
  }
  
  public Long getGameId() {
    return gameEntity != null ? gameEntity.getId() : null;
  }
  
  public boolean isRestored() {
    return isRestored;
  }
}