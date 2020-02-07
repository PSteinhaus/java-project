package vierGewinntUndChomp;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.MouseOverArea;

import java.awt.*;
import java.util.Arrays;

public class VierGewinntSlick extends BasicGame implements ComponentListener {
    private static final Color BACKGROUND_COLOR = new Color(10,46,10);
    private static final float ACCELLERATION = 0.3F;
    private final VierGewinntGraphical game;
    private Image chamber, coin1, coin2;
    static final int CELLSIZE = 70;
    static final int X_OFFSET = 40;
    static final int Y_OFFSET = 110;
    private boolean stopGame = false;
    private AppGameContainer app;
    private GameContainer container;
    private MouseOverArea[] areas;
    private int width, height;
    private int[][] stableCoins;
    private String playername1, playername2;
    private float activeCoinX = -1, activeCoinY, destinationY;
    private int destX=-1, destY=-1;
    private float velocity = 0;
    private int activePlayerNumber = -1;

    public VierGewinntSlick(int width, int height, String playername1, String playername2, VierGewinntGraphical game) {
        super("Vier gewinnt");
        this.game = game;
        this.width = width;
        this.height = height;
        stableCoins = new int[width][height];
        this.playername1 = playername1;
        this.playername2 = playername2;
        this.areas = new MouseOverArea[width];
        for (int[] numbers: stableCoins) {
            Arrays.fill(numbers, -1);
        }
    }

    public void init(GameContainer container) throws SlickException {
        if (container instanceof AppGameContainer) {
            this.app = (AppGameContainer)container;
        }
        this.container = container;
        container.setAlwaysRender(true);
        container.setTargetFrameRate(60);
        this.chamber= new Image("vierGewinntUndChomp/VierGewinntKammer.png");
        this.coin1  = new Image("vierGewinntUndChomp/coin2.png");
        this.coin2  = new Image("vierGewinntUndChomp/coin1.png");
        for(int i = 0; i < width; ++i) {
            this.areas[i] = new MouseOverArea(container, null, X_OFFSET+ i*CELLSIZE, X_OFFSET, CELLSIZE, height*CELLSIZE+(Y_OFFSET-X_OFFSET), this);
            this.areas[i].setNormalColor    (new Color(1.0F, 1.0F, 1.0F, 0.0F));
            this.areas[i].setMouseOverColor (new Color(1.0F, 1.0F, 1.0F, 0.2F));
            this.areas[i].setMouseDownColor (new Color(1.0F, 1.0F, 1.0F, 0.4F));
        }
        if(!myTurn())
            for (int j = 0; j < width; ++j)
                this.areas[j].setAcceptingInput(false);
    }

    private void drawCoin(int playernumber, float x, float y, Graphics g) {
        if(playernumber == 0) g.drawImage(coin1,x,y);
        if(playernumber == 1) g.drawImage(coin2,x,y);
    }

    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setBackground(BACKGROUND_COLOR);

        for(int i = 0; i < width; ++i) {
            this.areas[i].render(container, g);
        }
        // draw the active coin
        if(activePlayerNumber != -1) {  // (if it exists)
            //System.out.println("active coin exists");
            drawCoin(activePlayerNumber,activeCoinX,activeCoinY,g);
        }
        // draw the stable coins
        for(int i=0; i<width; i++)
            for(int j=0; j<height; j++) {
                drawCoin(stableCoins[i][j],X_OFFSET+i*CELLSIZE, Y_OFFSET+(height-1-j)*CELLSIZE, g);
            }

        // draw the chambers
        for(int i = 0; i < width; ++i)
            for(int j = 0; j < height; ++j)
                g.drawImage(chamber, X_OFFSET+i*CELLSIZE, Y_OFFSET+j*CELLSIZE);
    }

    public void update(GameContainer gc, int delta) throws SlickException
    {
        if(stopGame)
            gc.exit();
        if(activePlayerNumber != -1) {
            if(activeCoinY < destinationY) {
                // move the coin downwards
                velocity += ACCELLERATION;
                activeCoinY += velocity;
                if(activeCoinY >= destinationY) { // check whether you've reached your destination
                    addStableCoin();              // add this coin to the collection of coins to be rendered each turn
                }
            }
        }
    }

    private void addStableCoin() {
        velocity = 0;                 // reset the velocity for next time
        stableCoins[destX][destY] = activePlayerNumber;
        System.out.println("stable coin added at "+destX+","+destY);
        activeCoinX = -1;
        activeCoinY = -1;
        // reenable Input (if it wasn't your turn)
        if(game.getPlayernumber(game.getMe().getName())!=activePlayerNumber)
            for (int j = 0; j < width; ++j)
                this.areas[j].setAcceptingInput(true);
        activePlayerNumber = -1;    // it's over now
    }

    void stopGame() {
        stopGame=true;
    }

    void showTurn(int x, int y, Spieler player) {
        // the server just told you that a move happened, visualize it
        // set the parameters for the new coin
        System.out.println("now showing a turn: "+x+","+y);
        destX = x; destY = y;
        activeCoinX = X_OFFSET+x*CELLSIZE;
        activeCoinY = Y_OFFSET;
        destinationY = Y_OFFSET+(height-1-y)*CELLSIZE;
        activePlayerNumber = game.getPlayernumber(player.getName());  // encodes the color of the coin
        if(activeCoinY==destinationY) addStableCoin();  //dirty fix
    }

    @Override
    public void componentActivated(AbstractComponent source) {
        if(myTurn())
            for(int i = 0; i < width; ++i) {
                if (source == this.areas[i]) {
                    if(game.takeMove(i)) {  // test whether this move is accepted
                        // set the Activity-Zones to invisible
                        this.areas[i].mouseMoved(0, 0, 1, 1);
                        for (int j = 0; j < width; ++j)
                            this.areas[j].setAcceptingInput(false);
                    }
                }
            }
    }

    private boolean myTurn() {
        return game.activePlayer == game.getMe();
    }
}
