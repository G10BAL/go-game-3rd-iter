package edu.university.go.protocol;

public class ServerResponse {

  private final ResponseStatus status;
  private final String message;

  public ServerResponse(ResponseStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public ResponseStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String serialize() {
    return status + " " + message;
  }

  public static ServerResponse ok(String msg) {
    return new ServerResponse(ResponseStatus.OK, msg);
  }

  public static ServerResponse error(String msg) {
    return new ServerResponse(ResponseStatus.ERROR, msg);
  }
}
