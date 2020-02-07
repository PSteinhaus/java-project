package chatServerUndClient;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;
import vierGewinntUndChomp.*;

public class GameSession {
    // acts as a container for who joined this session and what it's about
    private String   nameOfGame = null;
    private ChatServerThread[] usersJoined = null;
    private int      numberOfPlayers = 0;
    private int      minPlayerCount  = 0;
    private int      id = -1; // for the server to be able to identify you easily
    private boolean  stopped = false;   // whether the game session has been closed (and told it's users about it)
    private Spiel game = null;

    GameSession(String _nameOfGame, int _minPlayerCount, int maxPlayerCount, int _id) {
        nameOfGame = _nameOfGame;
        minPlayerCount = _minPlayerCount;
        usersJoined = new ChatServerThread[maxPlayerCount];
        id = _id;
    }

    boolean hasPlayer(String playerName) {
        for (int i=0; i<usersJoined.length; i++)
            if(usersJoined[i]!=null)
                if(usersJoined[i].getUsername().equals(playerName)) {
                    return true;
                }
        return false;
    }

    int getId() { return id; }

    private boolean isReady() {
        // returns whether the game is ready to be started
        return numberOfPlayers >= minPlayerCount;
    }

    public String getHost() {
        return usersJoined[0].getUsername();
    }

    public String getNameOfGame() { return nameOfGame; }

    synchronized boolean addPlayer(ChatServerThread playerThread) {
        if(playerThread==null) return false;
        // first check whether the player is already in the game
        for (ChatServerThread playerThreadJoined: usersJoined)
            if(playerThreadJoined==playerThread) return false;
        // then search a free slot and place the player there
        for (int i=0; i<usersJoined.length; i++)
            if(usersJoined[i]==null) {
                usersJoined[i] = playerThread;
                numberOfPlayers++;
                // tell the others that someone joined
                for (int j=0; j<usersJoined.length; j++)
                    if(usersJoined[j]!=null) {
                        usersJoined[j].sendJoin(playerThread.getUsername(), isReady());
                    }
                return true;
            }
        return false;
    }

    synchronized boolean removePlayer(ChatServerThread playerThread) {  // returns true if the host is removed
        boolean stopAtEnd = false;
        if(usersJoined[0] == playerThread) stopAtEnd = true; // if it's the host disband the whole session
        for (int i=0; i<usersJoined.length; i++)
            if(usersJoined[i] == playerThread) {
                usersJoined[i] = null;
                numberOfPlayers--;
                // tell the others that someone left
                for (int j=0; j<usersJoined.length; j++)
                    if(usersJoined[j]!=null) {
                        usersJoined[j].sendLeave(playerThread.getUsername(), isReady());
                    }
                break;
            }
        if(stopAtEnd) {
            stop();
            return true;
        }
        return false;
    }

    String[] getPlayerNames() {
        String[] names = new String[numberOfPlayers];
        int i=0;
        for (ChatServerThread player: usersJoined) {
            if(player!=null) {
                names[i++] = player.getUsername();
            }
        }
        return names;
    }

    void startGame(int width, int height) {
        // first get all the players names (the games may need this info)
        String[] names = getPlayerNames();
        // tell all players to start the game!
        for (ChatServerThread player: usersJoined) {
            if(player!=null) {
                player.sendStartOfGame(nameOfGame, width, height, names);
            }
        }

        // start the game (server-side)
        switch (nameOfGame) {
            case "Chomp":
                game = new FutternGraphical(width,height,names[0],names[1],this);
                break;
            case "Vier gewinnt":
                game = new VierGewinntGraphical(width,height,names[0],names[1],this);
                break;
        }
    }

    void stop() {
        if(stopped) return; // if already stopped don't do anything
        // tell all users that the session is now closed
        for (ChatServerThread thread: usersJoined) {
            if(thread!=null)
            thread.joinedSessionClosed();
        }
        stopped = true;
        if (game!=null) {
            game.stopGame();
            game = null;
        }
    }

    boolean started() { return game != null; }

    void takePlayerInput(ChatServerThread player) {
        // a player just sent an input for the game, try to follow it
        if(started())
            game.receiveInput(player);
    }

    public void sendGameOutput(byte[] data) {
        // something happened in the game, send an update to the players
        for (ChatServerThread player: usersJoined) {
            if(player!=null) {
                System.out.println("sending GameOutput");
                player.receiveGameUpdate(data);
            }
        }
    }
}
