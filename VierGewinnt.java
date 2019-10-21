import java.util.Scanner;

public class VierGewinnt extends Spiel {
	private final int PLAYERNUMBER = 2;	// Anzahl der Spieler
	private final int WIDTH = 7;		// Breite des Spielbretts
	private final int HEIGHT = 6;		// Höhe des Spielbretts

	VierGewinnt() {
		spieler = new Spieler[PLAYERNUMBER];
		// lass einen Scanner den Kommandozeilen-input mitlesen
		Scanner scanner = new Scanner(System.in);
		System.out.println("Willkommen im Vier-Gewinnt-Prototypen!");
		// erfasse die Namen der Spieler
		for(int i=0; i<PLAYERNUMBER; i++) {
			System.out.print("Spieler "+(i+1)+", gib deinen Namen ein: ");
			String newName = scanner.next();
			System.out.print("Wähle dein Symbol (genau ein Zeichen): ");
			Character newSymbol = scanner.next().charAt(0);
			spieler[i] = new Spieler(newName,newSymbol);
		}
		spielfeld = new VierGewinntSpielfeld(WIDTH,HEIGHT);
		spielfeld.render();	// gib das leere Anfangs-Spielfeld aus
	};

	public void startRound() {
		// lass alle Spieler nacheinander ziehen
		for(int i=0; i<spieler.length; i++) {
			System.out.println(spieler[i].getName()+" ist am Zug.");
			takeTurn(spieler[i]);
			spielfeld.render();	// gib das neue Spielfeld aus
			// teste auf Sieg (oder Unentschieden)
			checkForEndOfGame();
		}
	};

	public void takeTurn(Spieler spieler) {
		// Lass einen Scanner den Kommandozeilen-input mitlesen
		Scanner scanner = new Scanner(System.in);
		int x,y;
		while(true) {
			// bitte um Eingabe des Zuges
			System.out.print("Wähle eine Spalte: ");
			// lies die Zahl und platziere den Stein
			x = scanner.nextInt() -1;
			y = ((VierGewinntSpielfeld)spielfeld).placeStone(spieler, x);
			if( y >= 0 ) break;	// falls der Stein platziert wurde akzeptiere den Zug
			System.out.println("Diese Spalte ist bereits voll.");
		}
		pushTurn(spieler,x,y);	// protokolliere den Zug
	}

	private void checkForEndOfGame() {
		// teste auf Sieg
		Spieler winner = ((VierGewinntSpielfeld)spielfeld).checkForWinner();
		if( winner != null ) {
			System.out.println(winner.getName()+" hat gewonnen!");
			System.exit(0);
		}
		// teste auf Unentschieden
		else if( ((VierGewinntSpielfeld)spielfeld).checkDrawCondition() ) {
			System.out.println("Unentschieden!");
			System.exit(0);
		}
	}
}