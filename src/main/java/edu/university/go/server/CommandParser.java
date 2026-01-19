package edu.university.go.server;

import edu.university.go.protocol.Command;
import edu.university.go.protocol.CommandType;

public class CommandParser {

  private CommandParser() {
    // utility class
  }

  public static Command parse(String input) {
    if (input == null || input.isBlank()) {
      return new Command(CommandType.UNKNOWN, new String[0]);
    }

    String[] tokens = input.trim().split("\\s+");
    String keyword = tokens[0].toUpperCase();

    CommandType type =
        switch (keyword) {
          case "JOIN" -> CommandType.JOIN;
          case "MOVE" -> CommandType.MOVE;
          case "PASS" -> CommandType.PASS;
          case "RESIGN" -> CommandType.RESIGN;
          case "QUIT" -> CommandType.QUIT;
          default -> CommandType.UNKNOWN;
        };

    String[] args = new String[Math.max(0, tokens.length - 1)];
    if (tokens.length > 1) {
      System.arraycopy(tokens, 1, args, 0, args.length);
    }

    return new Command(type, args);
  }
}
