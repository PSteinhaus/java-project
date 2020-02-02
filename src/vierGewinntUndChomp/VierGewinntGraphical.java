package vierGewinntUndChomp;

import chatServerUndClient.GameSession;
import chatServerUndClient.Helper;
import org.newdawn.slick.*;

import java.io.IOException;

public class VierGewinntGraphical extends Spiel implements Runnable {
    private final int PLAYERNUMBER = 2;	// Anzahl der Spieler
    private VierGewinntSlick game = null;
    private Thread       thread = null;
    private int width, height;
    private String playername1, playername2;

    // for the server
    public VierGewinntGraphical(int width, int height, String playername1, String playername2, GameSession session) {
        this.session = session;
        this.isServer = true;
        this.spieler = new Spieler[PLAYERNUMBER];
        spieler[0] = new Spieler(playername1, 'x', true);
        spieler[1] = new Spieler(playername2, 'o', true);
        spielfeld = new FutternSpielfeld(width,height);
        // start the graphical game
        //startSlick(width,height,playername1,playername2);
    }

    // for the client
    public VierGewinntGraphical(int width, int height, String playername1, String playername2) {
        this.session = null;
        this.isServer = false;
        this.spieler = new Spieler[PLAYERNUMBER];
        spieler[0] = new Spieler(playername1, 'x', true);
        spieler[1] = new Spieler(playername2, 'o', true);
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
            game = new VierGewinntSlick(width,height,playername1,playername2);
            AppGameContainer container = new AppGameContainer(game);
            container.setDisplayMode(800, 600, false);
            container.start();
        } catch (SlickException var2) {
            var2.printStackTrace();
        }
    }

    public void stopGame() {game.stopGame();};

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
        Spieler loser = spielfeld.checkForWinner();
        if( loser != null ) {
            // TODO : zeige Sieger an (und beende das Spiel)
            //System.out.println(loser.getName()+" hat verloren!");
            //System.exit(0);
        }
    }

    @Override
    protected void integrateTurn(Turn turn) {
        // try to add it to the game and if this works tell so and push the turn on top of your stack
        boolean accepted = ((FutternSpielfeld)spielfeld).integrateTurn(turn);
        if(accepted) {
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
            else
                showTurn(turn);
        }

    }

    private void showTurn(Turn turn) {
        // the server just told you that a move happened, visualize it
    }
}
