# Go-Game - Iteration 2

This is the second iteration of the Go-Game project. In this version, the UI implemented with JavaFX and board size is a parametr

## How to Run

### Client-Server
The game works in a client-server mode.
#### Server
```
mvn exec:java -Dexec.mainClass="edu.university.go.server.ServerMain" -Dexec.args="{port} {size}"
```

#### Client
```
mvn javafx:run
```

#### BOT
Bot also can be added instead of one of the players
To run bot:
```
mvn exec:java -Dexec.mainClass="edu.university.go.client.BotClient"
```
Or with optional parameters:
```
mvn exec:java -Dexec.mainClass="edu.university.go.client.BotClient" -Dexec.args="{host} {port}"
```

## Documentation

Documentation is generated with JavaDoc and can be accessed by ip-adress
Run local server for docs
```
python3 -m http.server 8000 --directory target/reports/apidocs
```

```
http://localhost:8000
```
Or maven
```
mvn javadoc:javadoc -q && xdg-open target/reports/apidocs/index.html
```

## Design Patterns Used

* **Singleton**: The server is created only once to ensure a single point of control.
* **Observer**: Clients subscribe to server updates, allowing real-time communication of game state.
* **DTO (Data Transfer Object)**: Data is transferred between server and clients using separate objects to encapsulate the information.
* **Protocol / Command**: Commands are used to standardize communication between the client and server. Each action (e.g., placing a stone, passing a turn) is represented as a command or protocol message, allowing clear separation of game logic and network communication.

## Some UML-diagrams
* Class diagram
* Sequence diagram
* State diagrams
  
![Class](uml/ClassDiagram.png)
![Class2](uml/ClassDiagram_ClientServer.png)
![Squence](uml/Sequence.png)
![State](uml/State.png)
![State2](uml/KoRuleState.png)
