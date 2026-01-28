package edu.university.go.server;

import edu.university.go.db.GameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "go.mode",
    havingValue = "server",
    matchIfMissing = true
)
public class ServerRunner implements CommandLineRunner {

    private final GameService gameService;

    @Value("${go.mode:server}")
    private String mode;

    @Value("${go.server.port:9999}")
    private int port;

    @Value("${go.server.boardSize:19}")
    private int boardSize;

    @Value("${go.server.autoStart:true}")
    private boolean autoStart;

    public ServerRunner(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void run(String... args) throws Exception {

        if (!"server".equalsIgnoreCase(mode)) {
            System.out.println("[ServerRunner] go.mode=" + mode + " → server NOT started");
            return;
        }

        if (!autoStart) {
            System.out.println("Server auto-start is disabled");
            return;
        }

        // Restore by ID
        if (args.length > 0 && "--restore".equals(args[0])) {
            if (args.length < 2) {
                System.err.println("Usage: --restore <game_id> [port]");
                return;
            }

            Long gameId = Long.parseLong(args[1]);

            if (args.length >= 3) {
                try {
                    port = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {}
            }

            restoreGame(gameId, port);
            return;
        }

        // Restore last active
        if (args.length > 0 && "--restore-last".equals(args[0])) {
            if (args.length >= 2) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }

            restoreLastActiveGame(port);
            return;
        }

        // Normal args
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        if (args.length >= 2) {
            try {
                boardSize = Integer.parseInt(args[1]);
                if (boardSize != 9 && boardSize != 13 && boardSize != 19) {
                    boardSize = 19;
                }
            } catch (NumberFormatException ignored) {}
        }

        startNewGame(port, boardSize);
    }

    private void startNewGame(int port, int boardSize) throws Exception {
        System.out.println("Go Game Server (Spring Boot)");
        System.out.println("Mode: SERVER");
        System.out.println("New Game");
        System.out.println("Port: " + port);
        System.out.println("Board Size: " + boardSize + "x" + boardSize);
        System.out.println("Database: ENABLED (H2)");
        System.out.println("H2 Console: http://localhost:8080/h2-console");

        Server server = new Server(port, boardSize, gameService);
        gameService.printStatistics();
        server.start();
    }

    private void restoreGame(Long gameId, int port) throws Exception {
        System.out.println("Go Game Server (Spring Boot)");
        System.out.println("Mode: SERVER");
        System.out.println("Restore Game #" + gameId);

        if (!gameService.gameExists(gameId)) {
            System.err.println("Game #" + gameId + " not found!");
            return;
        }

        var gameEntity = gameService.getGame(gameId);
        var loadedGame = gameService.loadGameWithCaptures(gameId);

        Server server = Server.createWithRestoredGame(
            port,
            loadedGame,
            gameEntity,
            gameService
        );

        server.start();
    }

    private void restoreLastActiveGame(int port) throws Exception {
        var lastGame = gameService.getAllGames().stream()
            .filter(g -> "ACTIVE".equals(g.getStatus()))
            .max((a, b) -> a.getUpdatedAt().compareTo(b.getUpdatedAt()))
            .orElse(null);

        if (lastGame == null) {
            System.err.println("No active games found");
            return;
        }

        restoreGame(lastGame.getId(), port);
    }
}
