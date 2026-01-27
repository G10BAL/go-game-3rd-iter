package edu.university.go.server;

import edu.university.go.board.Board;
import edu.university.go.board.Chain;
import edu.university.go.board.Color;
import edu.university.go.board.Point;
import edu.university.go.game.Game;
import edu.university.go.game.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotPlayer {

    private final Game game;
    private final Color botColor;
    private final Random random = new Random();

    public BotPlayer(Game game, Color botColor) {
        this.game = game;
        this.botColor = botColor;
    }

    public Move getNextMove() {
        Board board = game.getBoard();

        List<Point> defense = findCriticalLiberties(board, botColor);
        if (!defense.isEmpty()) {
            Point p = randomFrom(defense);
            return new Move(botColor, p.x, p.y, "Bot");
        }

        List<Point> attack = findCriticalLiberties(board, botColor.opposite());
        if (!attack.isEmpty()) {
            Point p = randomFrom(attack);
            return new Move(botColor, p.x, p.y, "Bot");
        }

        if (random.nextDouble() < 0.04) {
            return Move.pass(botColor, "Bot");
        }

        Move randomMove = randomValidMove(board);
        return randomMove != null ? randomMove : Move.pass(botColor, "Bot");
    }

    /**
     * Find critical liberties of chains with 1 liberty
     */
    private List<Point> findCriticalLiberties(Board board, Color color) {
        List<Point> result = new ArrayList<>();
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.get(x, y) == color && !visited[x][y]) {
                    Chain chain = board.getChain(new Point(x, y));
                    for (Point p : chain.getStones()) {
                        visited[p.x][p.y] = true;
                    }

                    if (board.countLiberties(chain) == 1) {
                        for (Point p : chain.getStones()) {
                            for (Point n : board.neighbors(p)) {
                                if (board.get(n.x, n.y) == Color.EMPTY) {
                                    if (isValidMove(board, n.x, n.y)) {
                                        result.add(n);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private boolean isValidMove(Board board, int x, int y) {
        Board copy = board.clone();
        return copy.placeStone(botColor, x, y);
    }

    private Move randomValidMove(Board board) {
        int size = board.getSize();

        for (int i = 0; i < 500; i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);

            if (board.get(x, y) == Color.EMPTY && isValidMove(board, x, y)) {
                return new Move(botColor, x, y, "Bot");
            }
        }
        return null;
    }

    private <T> T randomFrom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
