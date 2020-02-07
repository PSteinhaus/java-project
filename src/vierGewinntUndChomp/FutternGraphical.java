package vierGewinntUndChomp;

import chatServerUndClient.ChatClient;
import chatServerUndClient.GameSession;
import chatServerUndClient.Helper;
import org.lwjgl.Sys;
import org.newdawn.slick.*;

import java.io.IOException;

public class FutternGraphical extends Spiel implements Runnable {
    private final int PLAYERNUMBER = 2;	// Anzahl der Spieler
    private ChatClient client = null;
    private FutternSlick game = null;
    private Thread       thread = null;
    private int width, height;
    private String playername1, playername2;
    private Sound winnerSound = new Sound("vierGewinntUndChomp/crowd_reaction_positive_001.wav");
    private Sound loserSound  = new Sound("vierGewinntUndChomp/crowd_reaction_negative_001.wav");

    // for the server
    public FutternGraphical(int width, int height, String playername1, String playername2, GameSession session) throws SlickException {
        this.session = session;
        this.isServer = true;
        this.spieler = new Spieler[PLAYERNUMBER];
        spieler[0] = new Spieler(playername1, 'x', true);
        spieler[1] = new Spieler(playername2, 'o', true);
        activePlayer = spieler[0];
        spielfeld = new FutternSpielfeld(width,height);
        // start the graphical game
        //startSlick(width,height,playername1,playername2);
    }

    // for the client
    public FutternGraphical(int width, int height, String playername1, String playername2, ChatClient chat) throws SlickException {
        this.session = null;
        this.client = chat;
        this.isServer = false;
        this.spieler = new Spieler[PLAYERNUMBER];
        spieler[0] = new Spieler(playername1, 'x', true);
        spieler[1] = new Spieler(playername2, 'o', true);
        activePlayer = spieler[0];
        spielfeld = new FutternSpielfeld(width,height);
        this.width = width;
        this.height = height;
        this.playername1 = playername1;
        this.playername2 = playername2;
        // start the graphical game
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void run() {
        startSlick(width,height,playername1,playername2);
    }

    private void startSlick(int width, int height, String playername1, String playername2) {
        // start the graphical game
        try {
            game = new FutternSlick(width,height,playername1,playername2,this);
            AppGameContainer container = new AppGameContainer(game);
            container.setDisplayMode(width*FutternSlick.CELLSIZE+FutternSlick.X_OFFSET*2,
                    height*FutternSlick.CELLSIZE+FutternSlick.Y_OFFSET*2, false);
            container.start();
        } catch (SlickException var2) {
            var2.printStackTrace();
        }
    }

    public void stopGame() {if(game!=null) game.stopGame();};

    public void startRound() {
        // lass alle Spieler nacheinander ziehen
        for(int i=0; i<spieler.length; i++) {
            //System.out.println(spieler[i].getName()+" ist am Zug.");
            takeTurn(spieler[i]);
            //spielfeld.render();	// gib das neue Spielfeld aus
            // teste auf Sieg (oder Unentschieden)
            checkForEndOfGame();
        }
    };

    public void takeTurn(Spieler spieler) {
        int x = 0, y = 0;
        // TODO: wenn du dran bist schalte die Interaktion dafür ein, wenn nicht schalte sie aus und warte auf ein Update vom Server
    }

    public void checkForEndOfGame() {
        // teste auf Sieg
        System.out.println("testing for victory");
        Spieler loser = spielfeld.checkForWinner();
        if( loser != null ) {
            // TODO : zeige Sieger an (und beende das Spiel)
            if(!isServer) {
                client.writeChatOutput(loser.getName() + " hat verloren!");
                System.out.println(loser.getName() + " hat verloren!");
                if(loser.getName().equals(client.getUsername()))
                    loserSound.play();
                else
                    winnerSound.play();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
            stopGame();
        }
    }

    @Override
    protected boolean integrateTurn(Turn turn) {
        // try to add it to the game and if this works tell so and push the turn on top of your stack
        boolean accepted = ((FutternSpielfeld)spielfeld).integrateTurn(turn);
        if(accepted) {
            System.out.println("turn.player : "+turn.player.getName());
            if(!turn.player.getName().equals(getMe().getName())) activePlayer = getMe();
            else activePlayer = getOther();
            System.out.println("Aktiver Spieler: "+activePlayer.getName());
            pushTurn(turn);
            // if you're the server send an update to the others
            if(isServer)
                try {
                    session.sendGameOutput(Helper.serialize(turn));
                } catch (IOException ioe) {
                    stopGame();
                    System.out.println("Error reading received turn: "+ioe.getMessage());
                }
                // if not, then you have to react to it visually
            else {
                spielfeld.render();
                showTurn(turn);
            }
            checkForEndOfGame();
            return true;
        }
        else return false;
    }

    private void showTurn(Turn turn) {
        // the server just told you that a move happened, visualize it
        game.showTurn(turn.x, turn.y, turn.player);
    }

    private void sendTurnToServer(Turn turn) {
        // tell the server that you (a player) have done something
        client.sendGameUpdate(turn);
    }

    Spieler getMe() {
        if(isServer) {
            return spieler[0];
        }
        // find the player that I am
        for (Spieler player: spieler) {
            if(player!=null && player.getName().equals(client.getUsername())) return player;
        }
        return null;
    }

    Spieler getOther() {
        if(isServer) {
            return spieler[1];
        }
        // find the player that I am not
        for (Spieler player: spieler) {
            if(!player.getName().equals(client.getUsername())) return player;
        }
        return null;
    }

    int getPlayernumber(String searchedName) {
        // find the number of this player
        for (int i=0; i<spieler.length; i++) {
            if(spieler[i].getName().equals(searchedName)) return i;
        }
        return -1;
    }

    boolean takeMove(int x, int y) {
        // create a fitting turn and integrate it
        Spieler me = getMe();
        boolean accepted = ((FutternSpielfeld)spielfeld).movePossible(x,y);
        Turn turn = new Turn(getMe(), x,y);
        // falls der Zug akzeptiert wurde
        if( accepted ) {
            sendTurnToServer(turn);
            return true;
        }
        return false;
    }
}
