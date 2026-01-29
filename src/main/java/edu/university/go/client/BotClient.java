package edu.university.go.client;

import edu.university.go.board.Board;
import edu.university.go.board.Color;
import edu.university.go.game.EnhancedGameController;
import edu.university.go.game.Move;
import edu.university.go.server.BotPlayer;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bot client that connects to a Go server and plays automatically.
 */
public class BotClient {
    
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    
    private Color myColor = null;
    private int boardSize = 19;
    private EnhancedGameController controller;
    private BotPlayer botPlayer;
    
    private boolean gameStarted = false;
    private boolean myTurn = false;
    private StringBuilder boardBuffer = new StringBuilder();
    
    public BotClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        
        // Identify as bot IMMEDIATELY after connection, before any server messages
        System.out.println("[BotClient] Connected to " + host + ":" + port);
        System.out.println("[BotClient] Sending bot identification...");
        out.println("IDENTIFY_AS_BOT");
        out.flush();
    }
    
    public void start() {
        System.out.println("[BotClient] Starting bot client...");
        
        try {
            String line;
            while ((line = in.readLine()) != null) {
                handleServerMessage(line);
            }
        } catch (IOException e) {
            System.err.println("[BotClient] Connection error: " + e.getMessage());
        } finally {
            close();
        }
    }
    
    private void handleServerMessage(String message) {
        System.out.println("[BotClient] Received: " + message);
        
        if (message.trim().isEmpty()) {
            // End of board update
            if (boardBuffer.length() > 0) {
                parseAndUpdateBoard(boardBuffer.toString());
                boardBuffer = new StringBuilder();
            }
            return;
        }
        
        String trimmed = message.trim();
        
        if (trimmed.startsWith("BOARDSIZE ")) {
            boardSize = Integer.parseInt(trimmed.substring(10).trim());
            System.out.println("[BotClient] Board size: " + boardSize);
            initializeGame();
            
        } else if (trimmed.startsWith("COLOR ")) {
            String colorStr = trimmed.substring(6).trim();
            myColor = Color.valueOf(colorStr);
            System.out.println("[BotClient] I am playing as: " + myColor);
            
            // Initialize bot player
            if (controller != null) {
                botPlayer = new BotPlayer(controller.getGame(), myColor);
                System.out.println("[BotClient] Bot player initialized");
            }
            
        } else if (trimmed.startsWith("EVENT ")) {
            String event = trimmed.substring(6).trim();
            handleEvent(event);
            
        } else if (trimmed.startsWith("TURN ")) {
            String turnColor = trimmed.substring(5).trim();
            Color currentTurn = Color.valueOf(turnColor);
            myTurn = (currentTurn == myColor);
            System.out.println("[BotClient] Current turn: " + currentTurn + ", My turn: " + myTurn);
            
            if (gameStarted && myTurn && botPlayer != null) {
                // Schedule move in separate thread to not block message processing
                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Small delay
                        makeMove();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
            
        } else if (trimmed.startsWith("ERROR ")) {
            String error = trimmed.substring(6).trim();
            System.err.println("[BotClient] Server error: " + error);
            
        } else if (trimmed.startsWith("RESULT ")) {
            System.out.println("[BotClient] Game result: " + trimmed);
            gameStarted = false;
            
        } else if (trimmed.startsWith("CAPTURED ")) {
            // Log it
            System.out.println("[BotClient] " + trimmed);
            
        } else {
            // Board update line
            boardBuffer.append(message).append("\n");
        }
    }
    
    private void handleEvent(String event) {
        System.out.println("[BotClient] Event: " + event);
        
        if (event.contains("GAME_STARTED")) {
            gameStarted = true;
            System.out.println("[BotClient] Game started!");
            
        } else if (event.contains("MOVE_PLAYED")) {
            System.out.println("[BotClient] Move played, waiting for board update...");
        } else if (event.contains("GAME_ENDED")) {
            gameStarted = false;
            System.out.println("[BotClient] Game ended");
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            close();
            System.exit(0);
        }
    }
    
    private void initializeGame() {
        System.out.println("[BotClient] Initializing game with board size: " + boardSize);
        controller = new EnhancedGameController(boardSize, 7.5);
        
        // Initialize bot if we already know our color
        if (myColor != null) {
            botPlayer = new BotPlayer(controller.getGame(), myColor);
            System.out.println("[BotClient] Bot player initialized");
        }
    }
    
    private void parseAndUpdateBoard(String boardText) {
        if (controller == null) {
            System.err.println("[BotClient] Controller not initialized yet");
            return;
        }
        
        System.out.println("[BotClient] Parsing board update...");
        String[] lines = boardText.split("\n");
        
        Board board = controller.getBoard();
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
                    continue;
                }
                
                String stoneData = rowMatcher.group(2).trim();
                String[] stones = stoneData.split("\\s+");
                
                if (stones.length != boardSize) {
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
                        System.err.println("[BotClient] Error setting stone at (" + j + "," + rowNum + "): " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("[BotClient] Board updated");
    }
    
    private void makeMove() {
        if (botPlayer == null) {
            System.err.println("[BotClient] Bot player not initialized");
            return;
        }
        
        // Small delay 
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        Move move = botPlayer.getNextMove();
        System.out.println("[BotClient] Bot decided on move: " + move);
        
        String command;
        switch (move.type()) {
            case PLACE_STONE:
                command = String.format("MOVE %d %d %s", move.x(), move.y(), myColor);
                break;
            case PASS:
                command = "PASS";
                break;
            case RESIGN:
                command = "RESIGN";
                break;
            default:
                System.err.println("[BotClient] Unknown move type: " + move.type());
                return;
        }
        
        System.out.println("[BotClient] Sending: " + command);
        out.println(command);
        myTurn = false;
    }
    
    private void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[BotClient] Connection closed");
        } catch (IOException e) {
            System.err.println("[BotClient] Error closing connection: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9999;
        
        // Parse command line arguments
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[1]);
                System.exit(1);
            }
        }
        
        System.out.println("=".repeat(60));
        System.out.println("BOT CLIENT");
        System.out.println("=".repeat(60));
        System.out.println("Connecting to: " + host + ":" + port);
        System.out.println("Bot will play automatically");
        System.out.println("=".repeat(60));
        System.out.println();
        
        try {
            BotClient botClient = new BotClient(host, port);
            botClient.start();
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            System.err.println("Make sure the server is running at " + host + ":" + port);
            System.exit(1);
        }
    }
}