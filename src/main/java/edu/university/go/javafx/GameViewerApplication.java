package edu.university.go.javafx;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.db.GameEntity;
import edu.university.go.db.GameService;
import edu.university.go.db.MoveEntity;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class GameViewerApplication extends Application {

    private static final int CANVAS_SIZE = 600;
    private static final int MARGIN = 40;

    private static GameService staticGameService;
    private GameService gameService;

    private GameEntity currentGame;
    private List<MoveEntity> gameMoves;
    private Board board;
    private int currentMoveIndex = -1;
    private double cellSize;

    private Canvas boardCanvas;
    private Label gameInfoLabel;
    private Label moveInfoLabel;
    private Label scoreLabel;

    private Button firstButton;
    private Button prevButton;
    private Button nextButton;
    private Button lastButton;

    private ListView<GameEntity> gameListView;

    @Override
    public void init() {
        this.gameService = staticGameService;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Go Game Viewer");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setLeft(createGameListPanel());
        root.setCenter(createBoardPanel());
        root.setRight(createControlPanel());

        stage.setScene(new Scene(root, 1250, 720));
        stage.show();

        loadGamesList();
    }

    private VBox createGameListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("Game History");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        gameListView = new ListView<>();
        VBox.setVgrow(gameListView, Priority.ALWAYS);

        gameListView.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(GameEntity game, boolean empty) {
                super.updateItem(game, empty);
                if (empty || game == null) {
                    setText(null);
                } else {
                    setText(String.format(
                            "Game #%d (%dx%d)\nStatus: %s\nDate: %s",
                            game.getId(),
                            game.getBoardSize(),
                            game.getBoardSize(),
                            game.getStatus(),
                            game.getCreatedAt().toLocalDate()
                    ));
                }
            }
        });

        gameListView.setOnMouseClicked(e -> {
            GameEntity selected = gameListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadGame(selected);
            }
        });

        panel.getChildren().addAll(title, gameListView);
        return panel;
    }

    private VBox createBoardPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);

        moveInfoLabel = new Label("Select a game to start");
        moveInfoLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        boardCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);

        panel.getChildren().addAll(moveInfoLabel, boardCanvas);
        return panel;
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(320);
        panel.setStyle("-fx-background-color: #f5f5f5;");

        Label infoTitle = new Label("Game Information");
        infoTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        gameInfoLabel = new Label("No game loaded");
        gameInfoLabel.setWrapText(true);

        scoreLabel = new Label();
        scoreLabel.setStyle("-fx-font-weight: bold;");
        scoreLabel.setWrapText(true);

        Separator sep = new Separator();

        Label navTitle = new Label("Navigation");
        navTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        GridPane navGrid = new GridPane();
        navGrid.setHgap(8);
        navGrid.setVgap(8);
        navGrid.setAlignment(Pos.CENTER);

        firstButton = new Button("⏮ First");
        prevButton = new Button("◀ Previous");
        nextButton = new Button("Next ▶");
        lastButton = new Button("Last ⏭");

        firstButton.setOnAction(e -> goToFirstMove());
        prevButton.setOnAction(e -> goToPreviousMove());
        nextButton.setOnAction(e -> goToNextMove());
        lastButton.setOnAction(e -> goToLastMove());

        navGrid.add(firstButton, 0, 0);
        navGrid.add(prevButton, 1, 0);
        navGrid.add(nextButton, 0, 1);
        navGrid.add(lastButton, 1, 1);

        disableControls();

        panel.getChildren().addAll(
                infoTitle,
                gameInfoLabel,
                scoreLabel,
                sep,
                navTitle,
                navGrid
        );

        return panel;
    }

    private void loadGamesList() {
        if (gameService == null) return;
        gameListView.getItems().setAll(gameService.getAllGames());
    }

    private void loadGame(GameEntity game) {
        currentGame = game;
        gameMoves = gameService.getGameMoves(game.getId());

        board = new Board(game.getBoardSize());
        cellSize = (CANVAS_SIZE - 2 * MARGIN) / (double) (board.getSize() - 1);

        currentMoveIndex = -1;
        updateGameInfo();
        drawBoard();
        enableControls();
        updateMoveInfo();
    }

    private void updateGameInfo() {
        gameInfoLabel.setText(String.format(
                "Game ID: %d\nBoard: %dx%d\nStatus: %s\nMoves: %d\nCreated: %s",
                currentGame.getId(),
                currentGame.getBoardSize(),
                currentGame.getBoardSize(),
                currentGame.getStatus(),
                gameMoves.size(),
                currentGame.getCreatedAt().toLocalDate()
        ));

        if (currentGame.isFinished()) {
            scoreLabel.setText(String.format(
                    "Winner: %s\nBlack: %.1f | White: %.1f\nCaptured: B:%d W:%d",
                    currentGame.getWinner(),
                    currentGame.getBlackScore(),
                    currentGame.getWhiteScore(),
                    currentGame.getCapturedByBlack(),
                    currentGame.getCapturedByWhite()
            ));
        } else {
            scoreLabel.setText("");
        }
    }

    private void updateMoveInfo() {
        if (currentMoveIndex == -1) {
            moveInfoLabel.setText("Initial position");
            return;
        }

        MoveEntity move = gameMoves.get(currentMoveIndex);
        String emoji = move.getColor() == Color.BLACK ? "⚫" : "⚪";

        moveInfoLabel.setText(String.format(
                "%s Move %d: %s (%d, %d)",
                emoji,
                currentMoveIndex + 1,
                move.getColor(),
                move.getX(),
                move.getY()
        ));
    }

    private void replayToCurrentMove() {
        board = new Board(currentGame.getBoardSize());

        for (int i = 0; i <= currentMoveIndex; i++) {
            MoveEntity move = gameMoves.get(i);
            if ("MOVE".equals(move.getTurnType())) {
                board.placeStone(move.getColor(), move.getX(), move.getY());
            }
        }

        drawBoard();
        updateMoveInfo();
        updateButtons();
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();

        gc.setFill(javafx.scene.paint.Color.rgb(220, 179, 92));
        gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        gc.setStroke(javafx.scene.paint.Color.BLACK);

        for (int i = 0; i < board.getSize(); i++) {
            double p = MARGIN + i * cellSize;
            gc.strokeLine(p, MARGIN, p, MARGIN + (board.getSize() - 1) * cellSize);
            gc.strokeLine(MARGIN, p, MARGIN + (board.getSize() - 1) * cellSize, p);
        }

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.get(x, y) != Color.EMPTY) {
                    drawStone(gc, x, y, board.get(x, y));
                }
            }
        }
    }

    private void drawStone(GraphicsContext gc, int x, int y, Color color) {
        double cx = MARGIN + y * cellSize;
        double cy = MARGIN + x * cellSize;
        double r = cellSize * 0.45;

        gc.setFill(color == Color.BLACK
                ? javafx.scene.paint.Color.BLACK
                : javafx.scene.paint.Color.WHITE);

        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void goToFirstMove() { currentMoveIndex = -1; replayToCurrentMove(); }
    private void goToPreviousMove() { if (currentMoveIndex > -1) { currentMoveIndex--; replayToCurrentMove(); } }
    private void goToNextMove() { if (currentMoveIndex < gameMoves.size() - 1) { currentMoveIndex++; replayToCurrentMove(); } }
    private void goToLastMove() { currentMoveIndex = gameMoves.size() - 1; replayToCurrentMove(); }

    private void enableControls() {
        firstButton.setDisable(false);
        prevButton.setDisable(false);
        nextButton.setDisable(false);
        lastButton.setDisable(false);
        updateButtons();
    }

    private void disableControls() {
        firstButton.setDisable(true);
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        lastButton.setDisable(true);
    }

    private void updateButtons() {
        firstButton.setDisable(currentMoveIndex <= -1);
        prevButton.setDisable(currentMoveIndex <= -1);
        nextButton.setDisable(currentMoveIndex >= gameMoves.size() - 1);
        lastButton.setDisable(currentMoveIndex >= gameMoves.size() - 1);
    }

    public static void setGameServiceStatic(GameService gs) {
        staticGameService = gs;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
