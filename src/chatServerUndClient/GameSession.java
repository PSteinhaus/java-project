package chatServerUndClient;

public class GameSession {
    // acts as a container for who joined this session and what it's about
    private String   nameOfGame = null;
    private ChatServerThread[] usersJoined = null;
    private int      numberOfPlayers = 0;
    private int      id = -1; // for the server to be able to identify you easily

    GameSession(String _nameOfGame, int playerCount, int _id) {
        nameOfGame = _nameOfGame;
        usersJoined = new ChatServerThread[playerCount];
        id = _id;
    }

    int getId() { return id; }

    String getNameOfGame() { return nameOfGame; }

    boolean addPlayer(ChatServerThread playerThread) {
        for (int i=0; i<usersJoined.length; i++)
            if(usersJoined[i]==null) {
                usersJoined[i] = playerThread;
                numberOfPlayers++;
                // tell the others that someone joined
                for (int j=0; j<usersJoined.length; j++)
                    if(usersJoined[j]!=null) {
                        usersJoined[j].sendJoin(playerThread.getUsername());
                    }
                return true;
            }
        return false;
    }

    void removePlayer(ChatServerThread playerThread) {
        for (int i=0; i<usersJoined.length; i++)
            if(usersJoined[i] == playerThread) {
                usersJoined[i] = null;
                numberOfPlayers--;
                // tell the others that someone left
                for (int j=0; j<usersJoined.length; j++)
                    if(usersJoined[j]!=null) {
                        usersJoined[j].sendLeave(playerThread.getUsername());
                    }
                return;
            }
    }

    void startGame() {
        switch (nameOfGame) {
            case "Chomp":
                // TODO: start Chomp
                break;
            case "Vier gewinnt":
                // TODO: start Vier gewinnt
                break;
        }
    }

    void stop() {
        // tell all users that the session is now closed
        for (ChatServerThread thread: usersJoined) {
            thread.joinedSessionClosed();
        }
        // TODO: stop the game if already launched
    }
}
