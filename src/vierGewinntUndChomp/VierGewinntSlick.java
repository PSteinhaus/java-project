package vierGewinntUndChomp;

import org.newdawn.slick.*;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.MouseOverArea;

public class VierGewinntSlick extends BasicGame implements ComponentListener {
    private Image image;
    private boolean stopGame = false;
    private AppGameContainer app;
    private GameContainer container;
    private MouseOverArea[] areas;
    private int width, height;
    private int chosenX = -1;   // holds the number of the column chosen by the player as his next move
    private String playername1, playername2;

    public VierGewinntSlick(int width, int height, String playername1, String playername2) {
        super("Vier gewinnt");
        this.width = width;
        this.height = height;
        this.playername1 = playername1;
        this.playername2 = playername2;
        this.areas = new MouseOverArea[width];
    }

    public void init(GameContainer container) throws SlickException {
        if (container instanceof AppGameContainer) {
            this.app = (AppGameContainer)container;
        }
        this.container = container;
        this.image = new Image("testdata/logo.tga");
        for(int i = 0; i < width; ++i) {
            this.areas[i] = new MouseOverArea(container, this.image, 300, 100 + i * 100, 200, 90, this);
            this.areas[i].setNormalColor(new Color(1.0F, 1.0F, 1.0F, 0.8F));
            this.areas[i].setMouseOverColor(new Color(1.0F, 1.0F, 1.0F, 0.9F));
        }
    }

    public void render(GameContainer container, Graphics g) throws SlickException {
    }

    public void update(GameContainer gc, int delta) throws SlickException
    {
        if(stopGame)
            gc.exit();

        // Update code here
    }

    void stopGame() {
        stopGame=true;
    }

    @Override
    public void componentActivated(AbstractComponent source) {
        for(int i = 0; i < width; ++i) {
            if (source == this.areas[i]) {
                this.chosenX = i;
            }
        }
    }
}
