public class FutternSpielfeld extends Spielfeld {
	private Spieler[][] stones; // speichert die Spielsteine als 2D-Struktur (x - links nach rechts, y - oben nach unten)

	FutternSpielfeld(int width, int height) {
		super(width,height);
		stones = new Spieler[width][height]; // initialisiert alle Spielsteine auf "null"
	};

	private Spieler getPlayer(int x, int y) { return stones[x][y]; };

	private void setPlayer(Spieler player, int x, int y) { stones[x][y] = player; };

	private void checkLoseCondition() {
		// überprüfe ob der Platz oben links besetzt wurde
		winnerOrLoser = getPlayer(0,0);
	};

	public void render() {
		for(int j=0; j<height; j++) {
			for(int i=0; i<width; i++) {
				Spieler player = getPlayer(i,j);
				if( player == null ) {
					if( i==0 && j==0 )
						System.out.print("x");
					else
						System.out.print(".");
				}
				else
					System.out.print( player.getSymbol() );
				System.out.print(" ");
			}
			System.out.println();
		}
	};

	public boolean placeStone(Spieler player, int x, int y) {
		if( getPlayer(x,y) == null ) {				// falls der Platz frei ist
			for(int j=y; j<height; j++) 			// gehe über alle Felder rechts 
				for(int i=x; i<width; i++) {		// von und unter dem Ankerfeld
					if( getPlayer(i,j) == null )	// und falls diese Felder frei sind
						setPlayer(player,i,j);		// platziere Steine dort
				}
			checkLoseCondition();			// überprüfe, ob der andere Spieler damit gewonnen hat
			return true;
		};
		return false;
	};
}