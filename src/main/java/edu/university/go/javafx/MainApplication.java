package edu.university.go.javafx;

import edu.university.go.game.EnhancedGameController;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

  private EnhancedGameController controller;
  private GameBoardCanvas boardCanvas;
  private InfoPanel infoPanel;

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Go Game");

    // Get board size from command line parameters (default: 19)
    int boardSize = 19;
    double komi = 7.5;

    // Debug: print all parameters
    var allParams = getParameters();
    System.out.println("Named parameters: " + allParams.getNamed());
    System.out.println("Unnamed parameters: " + allParams.getUnnamed());
    System.out.println("Raw parameters: " + allParams.getRaw());

    Map<String, String> params = getParameters().getNamed();
    System.out.println("Checking for 'size' parameter...");

    if (params.containsKey("size")) {
      try {
        boardSize = Integer.parseInt(params.get("size"));
        if (boardSize != 9 && boardSize != 13 && boardSize != 19) {
          System.out.println("Invalid board size. Must be 9 13 or 19. Using default: 19");
          boardSize = 19;
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid board size format. Using default: 19");
      }
    } else {
      System.out.println("'size' parameter not found in named parameters");
    }

    if (params.containsKey("komi")) {
      try {
        komi = Double.parseDouble(params.get("komi"));
      } catch (NumberFormatException e) {
        System.out.println("Invalid komi format. Using default: 7.5");
      }
    }

    System.out.println(
        "Starting game with board size: " + boardSize + "x" + boardSize + ", komi: " + komi);

    // Create controller with specified parameters
    controller = new EnhancedGameController(boardSize, komi);

    // Add players to start the game
    controller.getGame().addPlayer("player-black");
    controller.getGame().addPlayer("player-white");

    // Create UI components
    boardCanvas = new GameBoardCanvas(controller);
    infoPanel = new InfoPanel(controller);

    // Layout
    BorderPane root = new BorderPane();
    root.setCenter(boardCanvas);
    root.setRight(infoPanel);

    // Scene
    Scene scene = new Scene(root, 1000, 800);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
