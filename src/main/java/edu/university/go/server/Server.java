package edu.university.go.server;

import edu.university.go.db.GameEntity;
import edu.university.go.db.GameService;
import edu.university.go.game.Game;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Go Game Server
 */
public class Server {

  private final int port;
  private final int boardSize;
  private final GameSession session;
  private final GameService gameService;

  public Server(int port, int boardSize, GameService gameService) {
    this.port = port;
    this.boardSize = boardSize;
    this.gameService = gameService;
    
    Game game = GameFactory.createGame(boardSize);
    this.session = new GameSession(game, gameService, boardSize);
    
    System.out.println("[Server] Initialized NEW game with database support");
  }
  
  private Server(int port, int boardSize, GameSession session, GameService gameService) {
    this.port = port;
    this.boardSize = boardSize;
    this.session = session;
    this.gameService = gameService;
    
    System.out.println("[Server] Initialized with RESTORED game");
  }
  
  public Server(int port, int boardSize) {
    this(port, boardSize, null);
    System.out.println("[Server] Initialized WITHOUT database support");
  }
  
  /**
   * Creates a server with a restored game
   */
  public static Server createWithRestoredGame(
      int port, 
      GameService.LoadedGame loadedGame,
      GameEntity gameEntity,
      GameService gameService) {
      
      GameSession session = new GameSession(
          loadedGame.game,
          gameEntity,
          gameService,
          gameService.getMoveCount(gameEntity.getId()),
          loadedGame.capturedByBlack,
          loadedGame.capturedByWhite
      );
      
      return new Server(port, gameEntity.getBoardSize(), session, gameService);
  }

  
  /**
   * Start the Go server
   */
  public void start() throws IOException {
    ServerSocket serverSocket = new ServerSocket(port);
    
    System.out.println("Go Server Started");
    System.out.println("Port: " + port);
    System.out.println("Board Size: " + boardSize + "x" + boardSize);
    
    if (gameService != null) {
      System.out.println("Database: ENABLED");
      System.out.println("Game ID: " + session.getGameId());
      
      if (session.isRestored()) {
        System.out.println("Mode: RESTORED GAME");
        System.out.println("Moves already played: " + gameService.getMoveCount(session.getGameId()));
      } else {
        System.out.println("Mode: NEW GAME");
      }
    } else {
      System.out.println("Database: DISABLED");
    }
    
    System.out.println("------------------------------------------------");
    System.out.println("Waiting for players to connect...\n");

    while (true) {
      Socket client = serverSocket.accept();
      System.out.println("[Server] New client connected: " + client.getInetAddress());
      new Thread(new ClientHandler(client, session)).start();
    }
  }
  
  public GameSession getSession() {
    return session;
  }
  
  public int getPort() {
    return port;
  }
  
  public int getBoardSize() {
    return boardSize;
  }
}