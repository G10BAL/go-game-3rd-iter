package edu.university.go.server;

import edu.university.go.board.Color;
import edu.university.go.game.Move;
import edu.university.go.game.PlayerType;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

class ClientHandler implements Runnable {

  private final Socket socket;
  private final GameSession session;
  private final String playerId = UUID.randomUUID().toString();
  
  private PlayerType playerType = PlayerType.HUMAN; // Default to HUMAN
  private boolean playerAdded = false;

  private PrintWriter out;
  private BufferedReader in;

  ClientHandler(Socket socket, GameSession session) {
    this.socket = socket;
    this.session = session;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      socket.setSoTimeout(50); // 50ms timeout
      
      try {
        String firstLine = in.readLine();
        if (firstLine != null && firstLine.trim().equalsIgnoreCase("IDENTIFY_AS_BOT")) {
          System.out.println("[ClientHandler] Client identified as BOT: " + playerId);
          playerType = PlayerType.BOT;
        } else if (firstLine != null) {
          System.out.println("[ClientHandler] Unexpected first message: " + firstLine);
        }
      } catch (java.net.SocketTimeoutException e) {
        System.out.println("[ClientHandler] No immediate identification, treating as HUMAN: " + playerId);
      }
      
      socket.setSoTimeout(0);
      
      send("CONNECTED " + playerId);
      session.addPlayer(playerId, this, playerType);
      playerAdded = true;
      System.out.println("[ClientHandler] Added player as " + playerType + ": " + playerId);

      // Continue processing commands
      String line;
      while ((line = in.readLine()) != null) {
        handleCommand(line);
      }

    } catch (IOException e) {
      System.err.println("[ClientHandler] Connection error: " + e.getMessage());
    }
  }

  public void handleCommand(String line) {
    String[] parts = line.trim().split("\\s+");
    String command = parts.length > 0 ? parts[0].toUpperCase() : "";

    // Handle game commands
    if ("MOVE".equals(command) && parts.length == 4) {
      // format: MOVE x y COLOR
      try {
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        Color color = Color.valueOf(parts[3]);
        session.handleMove(new Move(color, x, y, playerId));
      } catch (Exception e) {
        System.err.println("Error parsing MOVE command: " + e.getMessage());
        session.handleMove(new Move(Color.BLACK, -1, -1, playerId));
      }
    } else if ("PASS".equals(command)) {
      // format: PASS
      session.handlePass(playerId);
    } else if ("RESIGN".equals(command)) {
      // format: RESIGN
      session.handleResign(playerId);
    } else if ("IDENTIFY_AS_BOT".equals(command)) {
      // Just in case
      System.out.println("[ClientHandler] Received late IDENTIFY_AS_BOT command (ignored)");
    } else {
      // For invalid commands
      System.err.println("[ClientHandler] Unknown command: " + command);
    }
  }

  void send(String msg) {
    out.println(msg);
    out.flush(); // Ensure message is sent immediately
  }
}