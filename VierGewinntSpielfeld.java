public class VierGewinntSpielfeld extends Spielfeld {
	private Spieler[][] stones; // speichert die Spielsteine als 2D-Struktur (x - links nach rechts, y - unten nach oben)

	VierGewinntSpielfeld(int width, int height) {
		super(width,height);
		stones = new Spieler[width][height]; // initialisiert alle Spielsteine auf "null"
	};

	private Spieler getPlayer(int x, int y) { return stones[x][y]; };

	private void setPlayer(Spieler player, int x, int y) { stones[x][y] = player; };

	private void checkWinCondition(int xCenter, int yCenter) {
		// überprüfe die Umgebung der gegebenen Koordinate auf "vier in einer Reihe"
		// und speichere den Sieger (falls es einen gibt) in der Variable "winner" ab
		// horizontal
		checkLineForWinner(xCenter-3,yCenter, xCenter+3,yCenter);
		// vertikal
		checkLineForWinner(xCenter,yCenter-3, xCenter,yCenter+3);
		// diagonal links oben rechts unten
		checkLineForWinner(xCenter-3,yCenter+3, xCenter+3,yCenter-3);
		// diagonal links unten rechts oben
		checkLineForWinner(xCenter-3,yCenter-3, xCenter+3,yCenter+3);
	};

	private void checkLineForWinner(int x_start, int y_start, int x_end, int y_end) {
		if( winnerOrLoser != null ) return; // wenn es schon einen Sieger gibt suche nicht weiter
		// ansonsten starte eine Wanderung von Start- zu Zielpunkt
		SimpleLine.setCoords(x_start,y_start,x_end,y_end);

		int[] point = {x_start,y_start};
		int inEinerReihe = 0;
		Spieler steinBesitzer = null;
		while(true) {
			if( Helper.inRange(point[0],0,width-1) && Helper.inRange(point[1],0,height-1) ) { // stell sicher, dass die Werte innerhalb des Spielbretts liegen
				Spieler neuerSteinBesitzer = getPlayer(point[0],point[1]);
				if( neuerSteinBesitzer != null ) {
					if( neuerSteinBesitzer == steinBesitzer )
						inEinerReihe++;
					else
						inEinerReihe = 1;
				}
				else {
					inEinerReihe = 0;
				}
				steinBesitzer = neuerSteinBesitzer;
				if( inEinerReihe == 4 ) {
					winnerOrLoser = steinBesitzer;
					break;
				}
			}
			if( point[0] == x_end && point[1] == y_end ) break; // prüfe, ob der Endpunkt erreicht wurde
			point = SimpleLine.next();							// gehe zum nächsten Punkt
		}		
	};

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

	public void render() {
		for(int j=height-1; j>=0; j--) {
			for(int i=0; i<width; i++) {
				Spieler player = getPlayer(i,j);
				if( player == null )
					System.out.print(".");
				else
					System.out.print( player.getSymbol() );
				System.out.print(" ");
			}
			System.out.println();
		}
	};

	public int placeStone(Spieler player, int column) {
		// zuerst stelle sicher, dass die gewählte Spalte existiert
		if( Helper.inRange( column, 0,getWidth()-1 ) ) {
			// suche nach der niedrigsten freien Stelle in der Spalte
			for(int i=0; i<height; i++) {
				if( getPlayer(column,i) == null ) {	// falls der Platz frei ist
					setPlayer(player,column,i);		// platziere den Stein dort
					checkWinCondition(column,i);	// überprüfe, ob der Spieler damit gewonnen hat
					return i;
				}
			}
		}
		return -1;	// kein freier Platz übrig (Zug nicht akzeptiert)
	};
}

class SimpleLine {
	// gibt die Koordinaten von Punkten auf einer Geraden zwischen zwei gegebenen Punkten wieder
	// funktioniert nur für horizontale, vertikale und perfekt diagonale Geraden
	private static int[] startPoint = new int[2];
	private static int[] endPoint 	= new int[2];

	static public void setCoords(int x_start, int y_start, int x_end, int y_end) {
		startPoint[0]	= x_start;
		startPoint[1] 	= y_start;
		endPoint[0]	= x_end;
		endPoint[1]	= y_end;
	}

	static public int[] next() {
		// bewege die Punkte Richtung Ziel
		// x
		if( startPoint[0] < endPoint[0] )		startPoint[0]++;
		else if( startPoint[0] > endPoint[0] )	startPoint[0]--;
		// y
		if( startPoint[1] < endPoint[1] )		startPoint[1]++;
		else if( startPoint[1] > endPoint[1] )	startPoint[1]--;
		// gib den aktuellen Wegpunkt aus
		return startPoint;
	}
}