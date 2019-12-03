package vierGewinntUndChomp;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class VierGewinntSlick extends BasicGame {
    private boolean stopGame = false;

    public VierGewinntSlick(String title) {
        super(title);
    }

    public void init(GameContainer container) throws SlickException {
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
}
