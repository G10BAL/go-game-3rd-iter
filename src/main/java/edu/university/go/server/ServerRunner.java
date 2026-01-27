package edu.university.go.server;

import edu.university.go.db.GameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class ServerRunner implements CommandLineRunner {

    private final GameService gameService;

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
        if (!autoStart) {
            System.out.println("Server auto-start is disabled");
            return;
        }

        // Restore mode
        if (args.length > 0 && "--restore".equals(args[0])) {
            if (args.length < 2) {
                System.err.println("Usage: --restore <game_id> [port]");
                System.err.println("Example: --restore 5 9999");
                return;
            }
            
            Long gameId = Long.parseLong(args[1]);
            
            // If port
            if (args.length >= 3) {
                try {
                    port = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port: " + args[2] + ", using default " + port);
                }
            }
            
            restoreGame(gameId, port);
            return;
        }

        // Restore mode for last active game
        if (args.length > 0 && "--restore-last".equals(args[0])) {
            if (args.length >= 2) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port: " + args[1] + ", using default " + port);
                }
            }
            
            restoreLastActiveGame(port);
            return;
        }

        // Usual
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[0]);
            }
        }

        if (args.length >= 2) {
            try {
                boardSize = Integer.parseInt(args[1]);
                if (boardSize != 9 && boardSize != 13 && boardSize != 19) {
                    System.err.println("Board size must be 9, 13, or 19. Using default: 19");
                    boardSize = 19;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid board size: " + args[1]);
            }
        }

        startNewGame(port, boardSize);
    }
    
    /**
     * New game
     */
    private void startNewGame(int port, int boardSize) throws Exception {
        System.out.println("Go Game Server (Spring Boot)");
        System.out.println("New Game");
        System.out.println("Port: " + port);
        System.out.println("Board Size: " + boardSize + "x" + boardSize);
        System.out.println("Database: ENABLED (H2)");
        System.out.println("H2 Console: http://localhost:8080/h2-console");

        Server server = new Server(port, boardSize, gameService);
        
        gameService.printStatistics();
        
        server.start();
    }
    
    /**
     * Restore game by ID
     */
    private void restoreGame(Long gameId, int port) throws Exception {
        System.out.println("\nGo Game Server (Spring Boot)");
        System.out.println("Restore Game #" + gameId);
        
        // Whether exist
        if (!gameService.gameExists(gameId)) {
            System.err.println("\nGame #" + gameId + " not found!");
            System.err.println("\nAvailable games:");
            gameService.getAllGames().forEach(g -> 
                System.err.println("  - Game #" + g.getId() + " (" + g.getStatus() + 
                                 ", " + g.getBoardSize() + "x" + g.getBoardSize() + ")")
            );
            return;
        }
        
        var gameEntity = gameService.getGame(gameId);
        
        // Game info
        System.out.println("\nGame Information:");
        System.out.println("ID: " + gameEntity.getId());
        System.out.println("Board Size: " + gameEntity.getBoardSize() + "x" + gameEntity.getBoardSize());
        System.out.println("Status: " + gameEntity.getStatus());
        System.out.println("Created: " + gameEntity.getCreatedAt());
        System.out.println("Updated: " + gameEntity.getUpdatedAt());
        
        int moveCount = gameService.getMoveCount(gameId);
        System.out.println("Moves: " + moveCount);
        System.out.println("Captured - Black: " + gameEntity.getCapturedByBlack() + 
                         ", White: " + gameEntity.getCapturedByWhite());
        
        if ("FINISHED".equals(gameEntity.getStatus())) {
            System.out.println("\nWARNING: This game is already FINISHED!");
            System.out.println("Winner: " + gameEntity.getWinner());
            System.out.println("Score: Black " + gameEntity.getBlackScore() + 
                             " vs White " + gameEntity.getWhiteScore());
            System.out.println("\nYou can view it but cannot continue playing.");
        }
        
        System.out.println("\nRestoring game...");
        
        // Restore game
        // Game restoredGame = gameService.loadGame(gameId);
        GameService.LoadedGame loadedGame = gameService.loadGameWithCaptures(gameId);
        
        System.out.println("Game restored successfully!");
        System.out.println("Current turn: " + loadedGame.game.getCurrentTurn());
        System.out.println("Captured - Black: " + loadedGame.capturedByBlack + 
                        ", White: " + loadedGame.capturedByWhite);
        
        System.out.println("\nStarting server on port " + port + "...");
        System.out.println("H2 Console: http://localhost:8080/h2-console");
        
        Server server = Server.createWithRestoredGame(
            port, 
            loadedGame,
            gameEntity,
            gameService
        );
        
        server.start();
    }
    
    /**
     * Restore last ongoing game
     */
    private void restoreLastActiveGame(int port) throws Exception {
        System.out.println("Go Game Server (Spring Boot)");
        System.out.println("RESTORE LAST ACTIVE GAME");

        // Find last active game
        var activeGames = gameService.getAllGames().stream()
            .filter(g -> "ACTIVE".equals(g.getStatus()))
            .sorted((g1, g2) -> g2.getUpdatedAt().compareTo(g1.getUpdatedAt()))
            .toList();
        
        if (activeGames.isEmpty()) {
            System.err.println("No active games found in database!");
            System.err.println("\nAll games:");
            gameService.getAllGames().forEach(g -> 
                System.err.println("  - Game #" + g.getId() + " (" + g.getStatus() + ")")
            );
            return;
        }
        
        var lastGame = activeGames.get(0);
        System.out.println("Found last active game: #" + lastGame.getId());
        System.out.println("Updated: " + lastGame.getUpdatedAt());
        System.out.println("Board: " + lastGame.getBoardSize() + "x" + lastGame.getBoardSize());
        System.out.println();
        
        // Restore this game
        restoreGame(lastGame.getId(), port);
    }
}