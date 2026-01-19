package edu.university.go.javafx;

import edu.university.go.game.EnhancedGameController;
import edu.university.go.game.GameEvent;
import edu.university.go.game.GameObserver;
import edu.university.go.scoring.GameResult;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InfoPanel extends VBox implements GameObserver {

  private final EnhancedGameController controller;
  private final Label currentPlayerLabel;
  private final Label capturedBlackLabel;
  private final Label capturedWhiteLabel;
  private final Button passButton;
  private final Button resignButton;
  private final Label resultLabel;

  public InfoPanel(EnhancedGameController controller) {
    this.controller = controller;

    setSpacing(15);
    setPadding(new Insets(20));
    setPrefWidth(250);

    // Info about current player
    currentPlayerLabel = new Label();
    updateCurrentPlayer();

    // Captured stones
    capturedBlackLabel = new Label();
    capturedWhiteLabel = new Label();
    updateCapturedStones();

    // Buttlons
    passButton = new Button("Pass");
    passButton.setOnAction(
        e -> {
          controller.pass();
          updateCurrentPlayer();
        });

    resignButton = new Button("Resign");
    resignButton.setOnAction(
        e -> {
          controller.resign();
          showResult();
        });

    // Result
    resultLabel = new Label();
    resultLabel.setVisible(false);
    resultLabel.setWrapText(true);
    resultLabel.setMaxWidth(230);

    getChildren()
        .addAll(
            new Label("=== Go Game ==="),
            currentPlayerLabel,
            new Label(""),
            new Label("Captured stones:"),
            capturedBlackLabel,
            capturedWhiteLabel,
            new Label(""),
            passButton,
            resignButton,
            new Label(""),
            resultLabel);

    controller.addObserver(this);
  }

  private void updateCurrentPlayer() {
    currentPlayerLabel.setText(
        "Turn: "
            + (controller.getCurrentTurn() == edu.university.go.board.Color.BLACK
                ? "Black"
                : "White"));
  }

  private void updateCapturedStones() {
    capturedBlackLabel.setText("Black: " + controller.getCapturedByBlack());
    capturedWhiteLabel.setText("White: " + controller.getCapturedByWhite());
  }

  private void showResult() {
    GameResult result = controller.getGameResult();
    resultLabel.setText(result.toString());
    resultLabel.setVisible(true);
    passButton.setDisable(true);
    resignButton.setDisable(true);
  }

  @Override
  public void onGameEvent(GameEvent event) {
    updateCurrentPlayer();
    updateCapturedStones();

    if (event == GameEvent.GAME_ENDED) {
      showResult();
    }
  }
}
