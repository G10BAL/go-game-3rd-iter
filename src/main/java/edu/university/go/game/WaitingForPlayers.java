package edu.university.go.game;

import java.util.HashSet;
import java.util.Set;

class WaitingForPlayers implements GameState {

  private final Set<String> players = new HashSet<>();

  @Override
  public void addPlayer(Game game, String playerId) {
    players.add(playerId);
    if (players.size() == 2) {
      // Reset Ko rule at start
      game.getKoRule().reset();
      game.setState(new InProgress(players));
      game.notifyObservers(GameEvent.GAME_STARTED);
    }
  }

  @Override
  public void makeMove(Game game, Move move) {
    throw new IllegalStateException("Game has not started yet");
  }
}
