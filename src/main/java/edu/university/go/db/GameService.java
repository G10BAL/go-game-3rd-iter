package edu.university.go.db;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.board.Point;
import edu.university.go.game.Game;
import edu.university.go.game.Move;
import edu.university.go.game.MoveType;
import edu.university.go.game.PlayerType;
import edu.university.go.server.GameFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring for db
 */
@Service
public class GameService {
    
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    
    // Class for returning loaded game with captured info
    public static class LoadedGame {
        public final Game game;
        public final int capturedByBlack;
        public final int capturedByWhite;
        
        public LoadedGame(Game game, int capturedByBlack, int capturedByWhite) {
            this.game = game;
            this.capturedByBlack = capturedByBlack;
            this.capturedByWhite = capturedByWhite;
        }
    }
    
    public GameService(GameRepository gameRepository, MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }
    
    @Transactional
    public GameEntity createGame(int boardSize, PlayerType blackType, PlayerType whiteType) {
        GameEntity gameEntity = new GameEntity();
        gameEntity.setBoardSize(boardSize);
        gameEntity.setBlackType(blackType);
        gameEntity.setWhiteType(whiteType);
        gameEntity.setStatus("ACTIVE");
        gameEntity.setCapturedByBlack(0);
        gameEntity.setCapturedByWhite(0);
        
        GameEntity saved = gameRepository.save(gameEntity);
        System.out.println("[GameService] Created new game: " + saved);
        return saved;
    }
    
    @Transactional
    public MoveEntity saveMove(GameEntity gameEntity, int turnNumber, Move move, MoveType moveType) {
        MoveEntity moveEntity = new MoveEntity();
        moveEntity.setGame(gameEntity);
        moveEntity.setTurnNumber(turnNumber);
        moveEntity.setColor(move.color());
        
        switch (moveType) {
            case PLACE_STONE:
                moveEntity.setTurnType("MOVE");
                moveEntity.setX(move.x());
                moveEntity.setY(move.y());
                break;
            case PASS:
                moveEntity.setTurnType("PASS");
                break;
            case RESIGN:
                moveEntity.setTurnType("RESIGN");
                break;
        }
        
        MoveEntity saved = moveRepository.save(moveEntity);
        return saved;
    }
    
    public void updateGameStatus(Long gameId, String status, String winner, 
                                 Double blackScore, Double whiteScore,
                                 int capturedByBlack, int capturedByWhite) {
        try {
            gameRepository.updateGameStatus(
                gameId, 
                status, 
                winner, 
                blackScore, 
                whiteScore, 
                capturedByBlack, 
                capturedByWhite,
                LocalDateTime.now()
            );
            
            System.out.println("[GameService] Game #" + gameId + " status updated to: " + status);
        } catch (Exception e) {
            System.err.println("[GameService] Failed to update game status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Transactional(readOnly = true)
    public LoadedGame loadGameWithCaptures(Long gameId) {
        System.out.println("[GameService] Loading game #" + gameId);
        
        GameEntity gameEntity = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        Game game = GameFactory.createGame(gameEntity.getBoardSize());
        
        List<MoveEntity> moves = moveRepository.findByGameIdOrderByTurnNumberAsc(gameId);
        System.out.println("[GameService] Found " + moves.size() + " moves to replay");
        
        if (moves.isEmpty()) {
            System.out.println("[GameService] No moves to replay");
            return new LoadedGame(game, 0, 0);
        }
        
        ReplayResult result = replayMovesWithCaptures(game, moves);
        
        System.out.println("[GameService] Game loaded successfully");
        System.out.println("[GameService] Captured stones - Black: " + result.capturedByBlack + 
                         ", White: " + result.capturedByWhite);
        
        return new LoadedGame(game, result.capturedByBlack, result.capturedByWhite);
    }
    
    /**
     * Replay result
     */
    private static class ReplayResult {
        int capturedByBlack = 0;
        int capturedByWhite = 0;
    }
    
    /**
     * Replay moves and count captures
     */
    private ReplayResult replayMovesWithCaptures(Game game, List<MoveEntity> moves) {
        Board board = game.getBoard();
        ReplayResult result = new ReplayResult();
        
        for (MoveEntity moveEntity : moves) {
            try {
                if ("MOVE".equals(moveEntity.getTurnType())) {
                    Color color = moveEntity.getColor();
                    int x = moveEntity.getX();
                    int y = moveEntity.getY();
                    
                    // Count stones BEFORE placing the new stone
                    int whiteStonesBeforeMove = countStonesOnBoard(board, Color.WHITE);
                    int blackStonesBeforeMove = countStonesOnBoard(board, Color.BLACK);
                    
                    // Place stone (this will automatically capture enemy stones)
                    boolean placed = board.placeStone(color, x, y);
                    
                    if (!placed) {
                        System.err.println("[GameService] Failed to place stone at move #" + 
                                         moveEntity.getTurnNumber() + " (suicide or occupied)");
                        continue;
                    }
                    
                    // Count stones AFTER placing the new stone
                    int whiteStonesAfterMove = countStonesOnBoard(board, Color.WHITE);
                    int blackStonesAfterMove = countStonesOnBoard(board, Color.BLACK);
                    
                    // Calculate captured stones based on the difference
                    if (color == Color.BLACK) {
                        // Black placed a stone, check if white stones were captured
                        int whiteStonesCaptured = whiteStonesBeforeMove - whiteStonesAfterMove;
                        if (whiteStonesCaptured > 0) {
                            result.capturedByBlack += whiteStonesCaptured;
                            System.out.println("[GameService] Move #" + moveEntity.getTurnNumber() + 
                                             ": BLACK captured " + whiteStonesCaptured + " white stones");
                        }
                    } else {
                        // White placed a stone, check if black stones were captured
                        int blackStonesCaptured = blackStonesBeforeMove - blackStonesAfterMove;
                        if (blackStonesCaptured > 0) {
                            result.capturedByWhite += blackStonesCaptured;
                            System.out.println("[GameService] Move #" + moveEntity.getTurnNumber() + 
                                             ": WHITE captured " + blackStonesCaptured + " black stones");
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("[GameService] Error replaying move #" + moveEntity.getTurnNumber() + 
                                 ": " + e.getMessage());
            }
        }
        
        System.out.println("[GameService] Replayed " + moves.size() + " moves");
        System.out.println("[GameService] Total captured - Black: " + result.capturedByBlack + 
                         ", White: " + result.capturedByWhite);
        
        return result;
    }
    
    /**
     * Count stones of a specific color on the board
     */
    private int countStonesOnBoard(Board board, Color color) {
        int count = 0;
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.get(x, y) == color) {
                    count++;
                }
            }
        }
        return count;
    }
    
    @Transactional(readOnly = true)
    public List<GameEntity> getAllGames() {
        return gameRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public GameEntity getGame(Long gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
    
    @Transactional(readOnly = true)
    public List<MoveEntity> getGameMoves(Long gameId) {
        return moveRepository.findByGameIdOrderByTurnNumberAsc(gameId);
    }
    
    @Transactional
    public void deleteGame(Long gameId) {
        gameRepository.deleteById(gameId);
        System.out.println("[GameService] Deleted game #" + gameId);
    }
    
    @Transactional(readOnly = true)
    public boolean gameExists(Long gameId) {
        return gameRepository.existsById(gameId);
    }
    
    @Transactional(readOnly = true)
    public int getMoveCount(Long gameId) {
        return moveRepository.countByGameId(gameId);
    }
    
    @Transactional(readOnly = true)
    public void printStatistics() {
        List<GameEntity> games = getAllGames();
        
        long activeGames = games.stream()
            .filter(g -> "ACTIVE".equals(g.getStatus()))
            .count();
        
        long finishedGames = games.stream()
            .filter(g -> "FINISHED".equals(g.getStatus()))
            .count();
        
        System.out.println("Database Statistics");
        System.out.println("Total games: " + games.size());
        System.out.println("Active games: " + activeGames);
        System.out.println("Finished games: " + finishedGames);
    }
}