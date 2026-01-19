package edu.university.go.server;

import java.io.IOException;

/** Server Main Entry Point Default: port=9999, boardSize=19 */
public class ServerMain {

  public static void main(String[] args) throws IOException {
    int port = 9999;
    int boardSize = 19;

    if (args.length >= 1) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.println("Invalid port: " + args[0]);
        System.exit(1);
      }
    }

    if (args.length >= 2) {
      try {
        boardSize = Integer.parseInt(args[1]);
        if (boardSize != 9 && boardSize != 13 && boardSize != 19) {
          System.out.println("Board size must be 9, 13, or 19");
          System.exit(1);
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid board size: " + args[1]);
        System.exit(1);
      }
    }

    System.out.println("=== Go Game Server ===");
    System.out.println("Port: " + port);
    System.out.println("Board Size: " + boardSize + "x" + boardSize);

    Server server = new Server(port, boardSize);
    try {
      server.start();
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }
}
