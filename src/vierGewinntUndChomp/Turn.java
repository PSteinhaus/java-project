package vierGewinntUndChomp;

//trägt (speichert) alle Daten die einen Zug charakterisieren
public class Turn {
	public Spieler player;	// Der ziehende Spieler
	public int x,y;		// Das gewählte Feld (was dort passiert variiert von Spiel zu Spiel)
	
	Turn(Spieler player, int x, int y) {
		this.player = player;
		this.x = x;
		this.y = y;
	}
}