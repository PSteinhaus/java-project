public class VierGewinntSpielfeld extends Spielfeld {
	private Spieler[][] stones; // speichert die Spielsteine als 2D-Struktur (x - links nach rechts, y - unten nach oben)

	VierGewinntSpielfeld(int width, int height) {
		super(width,height);
		stones = new Spieler[width][height]; // initialisiert alle Spielsteine auf "null"
	};
	private Spieler getPlayer(int x, int y) { return stones[x][y]; };
	private void setPlayer(Spieler player, int x, int y) { stones[x][y] = player; };

	public void render() {};
	public int placeStone(Spieler player, int column) {
		// suche nach der niedrigsten freien Stelle in der Spalte
		for(int i=0; i<height; i++) {
			if( getPlayer(column,i) == null ) {	// falls der Platz frei ist
				setPlayer(player,column,i);		// platziere den Stein dort
				checkWinCondition(column,i);	// überprüfe, ob der Spieler damit gewonnen hat
				return i;
			}
		}
		return -1;	// kein freier Platz übrig
	};

	private void checkWinCondition(int xCenter, int yCenter) {
		// überprüfe die Umgebung der gegebenen Koordinate auf "vier in einer Reihe"
		// und speichere den Sieger (falls es einen gibt) in der Variable "winner" ab

		// TODO
	}

	public boolean checkDrawCondition() {
		// teste auf Unentschieden
		for( int i=0; i<getWidth(); i++ )
			for( int j=0; j<getHeight(); j++ ) {
				if( getPlayer(i,j) == null ) {
					return false;	// da ist noch Platz -> kein Unentschieden
				}
			}
		return true;
	};

	public Spieler checkForWinner() { return winner; };
}