package edu.university.go.game;

public interface GameState {

  void addPlayer(Game game, String playerId);

  void makeMove(Game game, Move move);
}
