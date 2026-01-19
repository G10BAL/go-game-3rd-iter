package edu.university.go.javafx;

import edu.university.go.board.Color;
import edu.university.go.client.Client;
import edu.university.go.game.EnhancedGameController;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientApplication extends Application {

  private Client client; // Handles network communication with the server
  private EnhancedGameController controller; // Local game controller (primarily for board access)
  private GameBoardCanvas boardCanvas; // UI component for rendering the game board
  private NetworkInfoPanel infoPanel; // UI component for displaying network and game info
  private Stage primaryStage; // Reference to the main application stage

  private String serverHost = "localhost"; // Default server host
  private int serverPort = 9999; // Default server port
  private int boardSize = 19; // Default board size

  private Color myColor = null; // The color assigned to this client by the server
  private StringBuilder boardBuffer =
      new StringBuilder(); // Buffer for accumulating board update lines from server
  private boolean gameStarted = false; // Flag indicating if the game has started
  private BorderPane root; // Main layout container

  // Local tracking of game state (since server manages logic, not local controller)
  private int capturedBlack = 0;
  private int capturedWhite = 0;
  private Color currentTurn = Color.BLACK;

  // Game result data
  private Color resultWinner = null;
  private double resultBlackScore = 0;
  private double resultWhiteScore = 0;
  private double resultMargin = 0;

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Go Game - Network Client");

    // Parse command-line parameters for customization
    Map<String, String> params = getParameters().getNamed();
    System.out.println("Parameters: " + params);

    if (params.containsKey("host")) {
      serverHost = params.get("host");
    }
    if (params.containsKey("port")) {
      try {
        serverPort = Integer.parseInt(params.get("port"));
      } catch (NumberFormatException e) {
        System.out.println("Invalid port format. Using default: " + serverPort);
      }
    }
    // NOTE: For network client, board size comes from server, NOT from parameters
    // The size parameter is ignored for network games
    System.out.println("Starting client with: host=" + serverHost + ", port=" + serverPort);
    System.out.println("Board size will be received from server...");

    // Initialize main layout
    root = new BorderPane();
    root.setStyle("-fx-background-color: #f5f5f5;");

    // Placeholder UI while connecting to the server
    VBox gamePanel = new VBox(20);
    gamePanel.setStyle("-fx-background-color: white; -fx-padding: 20;");
    gamePanel.setAlignment(javafx.geometry.Pos.CENTER);
    javafx.scene.control.Label waitingLabel =
        new javafx.scene.control.Label("Connecting to server...");
    waitingLabel.setStyle("-fx-font-size: 18; -fx-text-fill: gray;");
    gamePanel.getChildren().add(waitingLabel);
    root.setCenter(gamePanel);

    // Right panel for info
    infoPanel = new NetworkInfoPanel(this);
    root.setRight(infoPanel);

    // Create and set the scene
    Scene scene = new Scene(root, 1000, 800);
    primaryStage.setScene(scene);
    primaryStage.setOnCloseRequest(e -> closeConnection());
    primaryStage.show();

    this.primaryStage = primaryStage;

    // Attempt to connect to the server in a background thread
    connectToServerAutomatically();
  }

  /** Establishes a connection to the server in a background thread to prevent UI blocking. */
  private void connectToServerAutomatically() {
    Thread connectionThread =
        new Thread(
            () -> {
              try {
                System.out.println("[ClientApp] Connecting to " + serverHost + ":" + serverPort);
                client = new Client(serverHost, serverPort);
                client.connect();
                client.receiveAsync(this::handleServerMessage);

                Platform.runLater(
                    () -> {
                      System.out.println("[ClientApp] Connected successfully");
                      infoPanel.updateConnectionStatus("Connected", true);
                    });
              } catch (IOException e) {
                System.err.println("[ClientApp] Connection failed: " + e.getMessage());
                Platform.runLater(
                    () -> {
                      Alert alert = new Alert(Alert.AlertType.ERROR);
                      alert.setTitle("Connection Error");
                      alert.setContentText("Failed to connect: " + e.getMessage());
                      alert.showAndWait();
                    });
              }
            });
    connectionThread.setDaemon(true);
    connectionThread.start();
  }

  /**
   * Processes incoming messages from the server on the UI thread.
   *
   * @param message The raw message received from the server.
   */
  private void handleServerMessage(String message) {
    Platform.runLater(
        () -> {
          System.out.println("[ClientApp] Received: " + message);

          if (message.trim().isEmpty()) {
            // End of board update block
            if (boardBuffer.length() > 0) {
              if (!gameStarted) {
                System.out.println("[ClientApp] Received first board update - Game started!");
                gameStarted = true;
                // Create game display if not already created
                if (controller == null) {
                  System.out.println(
                      "[ClientApp] Creating display at board update because controller is null");
                  createGameDisplay();
                } else {
                  System.out.println(
                      "[ClientApp] Not creating display at board update, controller already exists with size: "
                          + boardSize);
                }
                infoPanel.enableGameControls();
              }
              parseAndUpdateBoard(boardBuffer.toString());
              boardBuffer = new StringBuilder();
            }
            return;
          }

          String trimmed = message.trim();

          if (trimmed.startsWith("CONNECTED ")
              || trimmed.startsWith("COLOR ")
              || trimmed.startsWith("BOARDSIZE ")
              || trimmed.startsWith("EVENT ")
              || trimmed.startsWith("ERROR ")
              || trimmed.startsWith("CAPTURED ")
              || trimmed.startsWith("TURN ")
              || trimmed.startsWith("RESULT ")) {

            int spaceIndex = trimmed.indexOf(' ');
            String command = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;
            String data = spaceIndex > 0 ? trimmed.substring(spaceIndex + 1).trim() : "";

            switch (command) {
              case "CONNECTED":
                infoPanel.updateConnectedAs(data);
                break;
              case "BOARDSIZE":
                try {
                  int newBoardSize = Integer.parseInt(data.trim());
                  System.out.println(
                      "[ClientApp] Received BOARDSIZE: "
                          + newBoardSize
                          + " (was: "
                          + boardSize
                          + ")");
                  boardSize = newBoardSize;
                  System.out.println("[ClientApp] Board size updated to: " + boardSize);
                  // Create game display once we know the board size
                  if (controller == null) {
                    System.out.println("[ClientApp] Creating display because controller is null");
                    createGameDisplay();
                  } else {
                    System.out.println(
                        "[ClientApp] Not creating display, controller already exists");
                  }
                } catch (NumberFormatException e) {
                  System.err.println("[ClientApp] Invalid boardsize: " + data);
                }
                break;
              case "COLOR":
                try {
                  myColor = Color.valueOf(data.trim());
                  infoPanel.updateColorAssigned(myColor);
                  // Update window title with assigned color
                  String colorEmoji = myColor == Color.BLACK ? "⚫" : "⚪";
                  primaryStage.setTitle("Go Game - " + colorEmoji + " " + myColor);
                } catch (IllegalArgumentException e) {
                  System.err.println("[ClientApp] Invalid color: " + data);
                }
                break;
              case "CAPTURED":
                try {
                  String[] parts = data.split(" ");
                  capturedBlack = Integer.parseInt(parts[0]);
                  capturedWhite = Integer.parseInt(parts[1]);
                  infoPanel.updateCapturedStones(capturedBlack, capturedWhite);
                } catch (Exception e) {
                  System.err.println("[ClientApp] Error parsing CAPTURED: " + e.getMessage());
                }
                break;
              case "TURN":
                try {
                  currentTurn = Color.valueOf(data.trim());
                  infoPanel.updateCurrentTurn(currentTurn);
                } catch (IllegalArgumentException e) {
                  System.err.println("[ClientApp] Invalid turn color: " + data);
                }
                break;
              case "RESULT":
                try {
                  // Format: RESULT WINNER BLACK_SCORE WHITE_SCORE MARGIN
                  String[] parts = data.split(" ");
                  if (parts.length >= 4) {
                    resultWinner = Color.valueOf(parts[0]);
                    resultBlackScore = Double.parseDouble(parts[1]);
                    resultWhiteScore = Double.parseDouble(parts[2]);
                    resultMargin = Double.parseDouble(parts[3]);
                    System.out.println(
                        "[ClientApp] Game result: "
                            + resultWinner
                            + " won (Black: "
                            + resultBlackScore
                            + ", White: "
                            + resultWhiteScore
                            + ", Margin: "
                            + resultMargin
                            + ")");
                  }
                } catch (Exception e) {
                  System.err.println("[ClientApp] Error parsing RESULT: " + e.getMessage());
                }
                break;
              case "EVENT":
                handleServerEvent(data);
                break;
              case "ERROR":
                System.err.println("[ClientApp] Server error: " + data);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Server Error");
                alert.setContentText(data);
                alert.showAndWait();
                break;
            }
          } else {
            // Accumulate lines for board update
            boardBuffer.append(message).append("\n");
          }
        });
  }

  /**
   * Handles specific game events from the server.
   *
   * @param eventData The event data string.
   */
  private void handleServerEvent(String eventData) {
    System.out.println("[ClientApp] Event: " + eventData);

    if (eventData.contains("GAME_STARTED")) {
      // Handled via board update
    } else if (eventData.contains("MOVE_PLAYED")) {
      // Board update follows
    } else if (eventData.contains("GAME_ENDED")) {
      // Game ended (after 2 passes or resign)
      gameStarted = false;
      infoPanel.disableGameControls();
      System.out.println("[ClientApp] Game has ended");

      // Show alert with game result
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("Game Finished");

      // Build content with result if available
      String content = "The game has ended.";
      if (resultWinner != null) {
        String winnerEmoji = resultWinner == Color.BLACK ? "⚫" : "⚪";
        content =
            String.format(
                "%s\n\nResult:\n%s %s wins\n\nScore:\nBlack: %.1f\nWhite: %.1f\nMargin: %.1f",
                content,
                winnerEmoji,
                resultWinner,
                resultBlackScore,
                resultWhiteScore,
                resultMargin);
      }
      alert.setContentText(content);
      alert.showAndWait();

      closeConnection();
      primaryStage.close();
    } else if (eventData.contains("GAME_FINISHED")) {
      gameStarted = false;
      infoPanel.disableGameControls();
    } else if (eventData.contains("PASS")) {

    } else if (eventData.contains("RESIGN")) {
      gameStarted = false;
      infoPanel.disableGameControls();

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("Game Finished");
      alert.setContentText("The opponent has resigned.");
      alert.showAndWait();

      closeConnection();
      primaryStage.close();
    }
  }

  /** Initializes the game display components once the game starts. */
  private void createGameDisplay() {
    if (controller == null) {
      System.out.println("[ClientApp] Creating game controller with size: " + boardSize);
      controller = new EnhancedGameController(boardSize, 7.5);
      boardCanvas = new GameBoardCanvas(controller);
      boardCanvas.setOnMoveClick(this::sendMoveToServer, false);
      root.setCenter(boardCanvas);
    }
  }

  /**
   * Sends a move command to the server, including the player's color.
   *
   * @param x The x-coordinate
   * @param y The y-coordinate
   */
  public void sendMoveToServer(int x, int y) {
    if (client == null || !client.isConnected() || myColor == null || !gameStarted) {
      System.err.println(
          "[ClientApp] Cannot send move: invalid state (check connection or color assignment)");
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Move Error");
      alert.setContentText(
          "Cannot make move: invalid state. Check if game started and color assigned.");
      alert.showAndWait();
      return;
    }
    // Send move
    String moveCommand = String.format("MOVE %d %d %s", x, y, myColor);
    System.out.println("[ClientApp] Sending move: " + moveCommand);
    client.send(moveCommand);
  }

  /** Sends a pass command to the server. */
  public void sendPassToServer() {
    if (client == null || !client.isConnected()) return;

    client.send("PASS");
  }

  /** Sends a resign command to the server. */
  public void sendResignToServer() {
    if (client == null || !client.isConnected()) return;

    client.send("RESIGN");
  }

  /**
   * Parses the board state text from the server and updates the local board. Uses setColor to
   * directly sync the state, handling captures by setting EMPTY where needed.
   *
   * @param boardText The multi-line board state string.
   */
  private void parseAndUpdateBoard(String boardText) {
    System.out.println("[ClientApp] Parsing board update...");
    String[] lines = boardText.split("\n");

    if (lines.length < boardSize) {
      System.err.println("[ClientApp] Invalid board format: too few lines");
      return;
    }

    if (controller == null) {
      createGameDisplay();
    }

    edu.university.go.board.Board board = controller.getBoard();

    Pattern rowPattern = Pattern.compile("^\\s*(\\d+)\\s+(.*)");
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;

      Matcher rowMatcher = rowPattern.matcher(line);
      if (rowMatcher.find()) {
        int rowNum;
        try {
          rowNum = Integer.parseInt(rowMatcher.group(1));
        } catch (NumberFormatException e) {
          System.err.println("[ClientApp] Skipping invalid row: " + line);
          continue;
        }
        String stoneData = rowMatcher.group(2).trim();
        String[] stones = stoneData.split("\\s+");

        if (stones.length != boardSize) {
          System.err.println("[ClientApp] Skipping row with wrong length: " + line);
          continue;
        }

        boolean validRow = true;
        for (String stoneChar : stones) {
          if (!stoneChar.equals("B") && !stoneChar.equals("W") && !stoneChar.equals(".")) {
            validRow = false;
            break;
          }
        }
        if (!validRow) {
          System.err.println("[ClientApp] Skipping non-board row (header?): " + line);
          continue;
        }

        for (int j = 0; j < stones.length; j++) {
          String stoneChar = stones[j];
          Color stoneColor;
          switch (stoneChar) {
            case "B":
              stoneColor = Color.BLACK;
              break;
            case "W":
              stoneColor = Color.WHITE;
              break;
            default:
              stoneColor = Color.EMPTY;
              break;
          }
          try {
            board.setColor(j, rowNum, stoneColor);
          } catch (Exception e) {
            System.err.println(
                "[ClientApp] Error setting stone at (" + j + "," + rowNum + "): " + e.getMessage());
          }
        }
      }
    }

    if (boardCanvas != null) {
      boardCanvas.redraw();
      System.out.println("[ClientApp] Board redrawn");
    } else {
      System.err.println("[ClientApp] boardCanvas is null, cannot redraw");
    }
  }

  /** Closes the connection to the server gracefully. */
  private void closeConnection() {
    if (client != null && client.isConnected()) {
      try {
        client.send("DISCONNECT");
        client.close();
      } catch (IOException e) {
        System.err.println("Error closing connection: " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
