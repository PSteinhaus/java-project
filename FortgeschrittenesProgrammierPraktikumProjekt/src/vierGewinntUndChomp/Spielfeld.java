package vierGewinntUndChomp;

public abstract class Spielfeld {
	protected int width, height;
	protected Spieler winnerOrLoser = null;	// das Spielfeld verwaltet die Siegbedingung, da in diesem Design das Spielfeld auch die interne Spielmechanik abbildet

	Spielfeld(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() { return width; };
	public int getHeight() { return height; };
	public abstract void render();
	public Spieler checkForWinner() { return winnerOrLoser; };
}
