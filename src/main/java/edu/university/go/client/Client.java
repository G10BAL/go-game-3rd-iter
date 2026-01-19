package edu.university.go.client;

import java.io.*;
import java.net.Socket;

public class Client {

  private final String host;
  private final int port;

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void connect() throws IOException {
    socket = new Socket(host, port);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
  }

  public void send(String command) {
    out.println(command);
  }

  public String receive() throws IOException {
    return in.readLine();
  }

  public void close() throws IOException {
    socket.close();
  }

  /** Check if client is connected. */
  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  /**
   * Start listening for messages from the server in a background thread. Calls the provided
   * callback when messages arrive.
   *
   * @param callback Function to call with incoming messages
   */
  public void receiveAsync(java.util.function.Consumer<String> callback) {
    Thread thread =
        new Thread(
            () -> {
              try {
                String line;
                while ((line = in.readLine()) != null) {
                  callback.accept(line);
                }
              } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
              }
            });
    thread.setDaemon(true);
    thread.start();
  }
}
