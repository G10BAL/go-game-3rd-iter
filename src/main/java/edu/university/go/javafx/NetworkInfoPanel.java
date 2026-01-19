package edu.university.go.javafx;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class NetworkInfoPanel extends VBox {
  private final Label connectionStatusLabel;
  private final Label colorLabel;
  private final Label currentPlayerLabel;
  private final Label capturedBlackLabel;
  private final Label capturedWhiteLabel;
  private final Button passButton;
  private final Button resignButton;

  public NetworkInfoPanel(ClientApplication clientApp) {
    setSpacing(15);
    setPadding(new Insets(20));
    setPrefWidth(250);
    setStyle("-fx-background-color: #ecf0f1;");

    // Connection Status
    connectionStatusLabel = new Label("⚫ Disconnected");
    connectionStatusLabel.setStyle(
        "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12;");

    colorLabel = new Label("⬜ Waiting for color...");
    colorLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

    currentPlayerLabel = new Label("Waiting for game...");
    currentPlayerLabel.setStyle("-fx-font-size: 12;");

    // Captured stones
    capturedBlackLabel = new Label("⚫ Black: 0");
    capturedBlackLabel.setStyle("-fx-font-size: 11;");
    capturedWhiteLabel = new Label("⚪ White: 0");
    capturedWhiteLabel.setStyle("-fx-font-size: 11;");

    // Buttons
    passButton = new Button("Pass");
    passButton.setPrefWidth(200);
    passButton.setDisable(true);
    passButton.setOnAction(e -> clientApp.sendPassToServer());

    resignButton = new Button("Resign");
    resignButton.setPrefWidth(200);
    resignButton.setDisable(true);
    resignButton.setOnAction(e -> clientApp.sendResignToServer());

    getChildren()
        .addAll(
            new Label("=== Network Game ==="),
            connectionStatusLabel,
            colorLabel,
            new Label(""),
            new Label("Game Info:"),
            currentPlayerLabel,
            new Label(""),
            new Label("Captured stones:"),
            capturedBlackLabel,
            capturedWhiteLabel,
            new Label(""),
            passButton,
            resignButton);
  }

  public void updateConnectionStatus(String status, boolean connected) {
    String icon = connected ? "🟢" : "🔴";
    connectionStatusLabel.setText(icon + " " + status);
    connectionStatusLabel.setStyle(
        "-fx-text-fill: "
            + (connected ? "#27ae60" : "#e74c3c")
            + "; -fx-font-weight: bold; -fx-font-size: 12;");
  }

  public void updateConnectedAs(String playerId) {
    colorLabel.setText("⚪ Player: " + playerId);
  }

  public void updateColorAssigned(edu.university.go.board.Color color) {
    String icon = color == edu.university.go.board.Color.BLACK ? "⚫" : "⚪";
    colorLabel.setText(icon + " Your color: " + color);
  }

  public void updateCapturedStones(int capturedBlack, int capturedWhite) {
    capturedBlackLabel.setText("⚫ Black: " + capturedBlack);
    capturedWhiteLabel.setText("⚪ White: " + capturedWhite);
  }

  public void updateCurrentTurn(edu.university.go.board.Color color) {
    String icon = color == edu.university.go.board.Color.BLACK ? "⚫" : "⚪";
    String colorText = color == edu.university.go.board.Color.BLACK ? "BLACK" : "WHITE";
    currentPlayerLabel.setText(icon + " Current turn: " + colorText);
  }

  public void enableGameControls() {
    passButton.setDisable(false);
    resignButton.setDisable(false);
  }

  public void disableGameControls() {
    passButton.setDisable(true);
    resignButton.setDisable(true);
  }
}
