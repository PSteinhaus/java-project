package vierGewinntUndChomp;

import org.newdawn.slick.*;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.MouseOverArea;

import java.util.Arrays;

public class FutternSlick extends BasicGame implements ComponentListener {
    //private static final Color BACKGROUND_COLOR = new Color(10,46,10);
    private final FutternGraphical game;
    private Image chamber, chamberDense;
    static final int CELLSIZE = 70;
    static final int X_OFFSET = 30;
    static final int Y_OFFSET = 30;
    private boolean stopGame = false;
    private AppGameContainer app;
    private GameContainer container;
    private MouseOverArea[][] areas;
    private int width, height;
    private int[][] fields;
    private String playername1, playername2;
    private int activePlayerNumber = -1;
    private Sound sound = new Sound("vierGewinntUndChomp/Home_Office_Kitchen_ChopFood_03.wav");

    public FutternSlick(int width, int height, String playername1, String playername2, FutternGraphical game) throws SlickException {
        super("Futtern");
        this.game = game;
        this.width = width;
        this.height = height;
        fields = new int[width][height];
        this.playername1 = playername1;
        this.playername2 = playername2;
        this.areas = new MouseOverArea[width][height];
        for (int[] numbers: fields) {
            Arrays.fill(numbers, -1);
        }
    }

    public void init(GameContainer container) throws SlickException {
        if (container instanceof AppGameContainer) {
            this.app = (AppGameContainer)container;
        }
        this.container = container;
        container.setAlwaysRender(true);
        container.setShowFPS(false);
        container.setTargetFrameRate(60);
        this.chamber= new Image("vierGewinntUndChomp/ChompField.png");
        this.chamberDense  = new Image("vierGewinntUndChomp/ChompFieldDense.png");
        for(int i = 0; i < width; ++i)
            for(int j = 0; j < height; ++j) {
                this.areas[i][j] = new MouseOverArea(container, chamber, X_OFFSET+ i*CELLSIZE, Y_OFFSET+ j*CELLSIZE, CELLSIZE, CELLSIZE, this);
                this.areas[i][j].setNormalColor    (new Color(1.0F, 1.0F, 1.0F, 0.8F));
                this.areas[i][j].setMouseOverColor (new Color(1.0F, 1.0F, 1.0F, 1.0F));
                this.areas[i][j].setMouseDownColor (new Color(1.0F, 1.0F, 1.0F, 0.6F));
            }
        if(!myTurn())
            for(int i = 0; i<height; i++)
                for (int j = 0; j < width; ++j)
                    this.areas[j][i].setAcceptingInput(false);
    }

    public void render(GameContainer container, Graphics g) throws SlickException {
        //g.setBackground(BACKGROUND_COLOR);

        for(int i = 0; i < width; ++i)
            for(int j = 0; j < height; ++j) {
                this.areas[i][j].render(container, g);
            }
    }

    void showTurn(int x, int y, Spieler player) {
        // turn all the selected fields to the player's color
        for(int j=y; j<height; j++) 			// gehe über alle Felder rechts
            for(int i=x; i<width; i++) {		// von und unter dem Ankerfeld
                if( fields[i][j] == -1 )	    // und falls diese Felder frei sind
                    setPlayer(player,i,j);		// platziere Steine dort
            }
        // reactivate all the fields that are still available (if it's your turn now)
        if(myTurn()) {
            System.out.println("it's my turn now");
            for (int j = 0; j < height; j++)            // gehe über alle Felder
                for (int i = 0; i < width; i++) {        //
                    if (fields[i][j] == -1)        // und falls diese Felder frei sind
                        this.areas[i][j].setAcceptingInput(true);   // gebe sie für Input frei
                }
        }
        // play a sound!
        sound.play();
    }

    private void setPlayer(Spieler player, int i, int j) {
        // turn the field over to this player
        if(player!=null)
            fields[i][j] = game.getPlayernumber(player.getName());
        else
            fields[i][j] = -1;
        // colorize it
        if(player==null) {
            this.areas[i][j].setNormalColor    (new Color(1.0F, 1.0F, 1.0F, 0.8F));
            this.areas[i][j].setMouseOverColor (new Color(1.0F, 1.0F, 1.0F, 1.0F));
            this.areas[i][j].setMouseDownColor (new Color(1.0F, 1.0F, 1.0F, 0.6F));
            this.areas[i][j].setAcceptingInput(true);   // activate it
        } else if (game.getPlayernumber(player.getName())==0) {
            this.areas[i][j].setNormalColor    (new Color(1.0F, 0.0F, 0.0F, 0.8F));
            this.areas[i][j].setMouseOverColor (new Color(1.0F, 0.0F, 0.0F, 1.0F));
            this.areas[i][j].setMouseDownColor (new Color(1.0F, 0.0F, 0.0F, 0.6F));
            this.areas[i][j].setAcceptingInput(false);  // deactivate it
        } else {
            this.areas[i][j].setNormalColor    (new Color(0.0F, 0.0F, 1.0F, 0.8F));
            this.areas[i][j].setMouseOverColor (new Color(0.0F, 0.0F, 1.0F, 1.0F));
            this.areas[i][j].setMouseDownColor (new Color(0.0F, 0.0F, 1.0F, 0.6F));
            this.areas[i][j].setAcceptingInput(false);  // deactivate it
        }
    }

    public void update(GameContainer gc, int delta) throws SlickException
    {
        if(stopGame)
            gc.exit();
    }

    void stopGame() {
        stopGame=true;
    }

    @Override
    public void componentActivated(AbstractComponent source) {
        if(myTurn())
            for(int i = 0; i < width; ++i)
                for(int j = 0; j < height; ++j) {
                    if (source == this.areas[i][j]) {
                        if(game.takeMove(i,j)) {  // test whether this move is accepted
                            // set the Activity-Zones to invisible
                            this.areas[i][j].mouseMoved(0, 0, 1, 1);
                            for (int k = 0; k < width; ++k)
                                for(int l = 0; l < height; ++l)
                                    this.areas[k][l].setAcceptingInput(false);
                        }
                    }
                }
    }

    private boolean myTurn() {
        return game.activePlayer == game.getMe();
    }
}
