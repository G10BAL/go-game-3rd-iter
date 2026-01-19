package edu.university.go.server;

import edu.university.go.board.Color;
import edu.university.go.game.Move;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

class ClientHandler implements Runnable {

  private final Socket socket;
  private final GameSession session;
  private final String playerId = UUID.randomUUID().toString();

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

      session.addPlayer(playerId, this);
      send("CONNECTED " + playerId);

      String line;
      while ((line = in.readLine()) != null) {
        handleCommand(line);
      }

    } catch (IOException e) {
      send("ERROR: connection lost");
    }
  }

  public void handleCommand(String line) {
    String[] parts = line.trim().split("\\s+");
    String command = parts.length > 0 ? parts[0].toUpperCase() : "";

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
    } else {
      // For invalid commands
      session.handleMove(new Move(Color.BLACK, -1, -1, playerId));
    }
  }

  void send(String msg) {
    out.println(msg);
    out.flush(); // Ensure message is sent immediately
  }
}
