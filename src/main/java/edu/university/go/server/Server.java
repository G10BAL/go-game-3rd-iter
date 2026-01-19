package edu.university.go.server;

import edu.university.go.game.Game;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  private static Server instance;

  private final int port;
  private final int boardSize;
  private final GameSession session;

  // Singleton constructor
  private Server() {
    this(9999, 19);
  }

  // Constructor with parameters
  public Server(int port, int boardSize) {
    this.port = port;
    this.boardSize = boardSize;
    Game game = GameFactory.createGame(boardSize);
    session = new GameSession(game);
  }

  public static synchronized Server getInstance() {
    if (instance == null) {
      instance = new Server();
    }
    return instance;
  }

  public void start() throws IOException {
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("Go server started on port " + port);

    while (true) {
      Socket client = serverSocket.accept();
      new Thread(new ClientHandler(client, session)).start();
    }
  }
}
