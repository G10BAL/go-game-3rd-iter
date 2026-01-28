package edu.university.go.javafx;

import edu.university.go.db.GameService;
import javafx.application.Application;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Spring Boot command line runner that launches the Game Viewer JavaFX application.
 * This is activated when the viewer mode is enabled.
 */
@Component
@ConditionalOnProperty(
    name = "go.mode",
    havingValue = "viewer"
)
public class GameViewerRunner implements CommandLineRunner {

    private final GameService gameService;

    public GameViewerRunner(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Launch JavaFX application in a separate thread
        Thread viewerThread = new Thread(() -> {
            // Pass GameService to JavaFX application
            GameViewerApplication.setGameServiceStatic(gameService);
            Application.launch(GameViewerApplication.class, args);
        });
        viewerThread.setDaemon(false);
        viewerThread.start();
    }
}