package vierGewinntUndChomp;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class VierGewinnt extends Spiel {
	private final int PLAYERNUMBER = 2;	// Anzahl der Spieler

	public VierGewinnt() {
		spieler = new Spieler[PLAYERNUMBER];
		System.out.println("Willkommen im Vier-Gewinnt-Prototypen!");
		// erfasse die Namen der Spieler & die Dimensionen des Spielbretts
		int[] dimensions = getNamesAndDimensions();
		spielfeld = new VierGewinntSpielfeld(dimensions[0],dimensions[1]);
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
		int x,y;
		if( spieler.isHuman() ) {
			// Lass einen Scanner den Kommandozeilen-input mitlesen
			Scanner scanner = new Scanner(System.in);
			while(true) {
				// bitte um Eingabe des Zuges
				System.out.print("Wähle eine Spalte: ");
				// lies die Zahl und platziere den Stein
				x = scanner.nextInt() -1;
				y = ((VierGewinntSpielfeld)spielfeld).placeStone(spieler, x);
				if( y >= 0 ) break;	// falls der Stein platziert wurde akzeptiere den Zug
				System.out.println("Diese Spalte ist bereits voll.");
			}
		}
		else {	// falls der Spieler ein Computer ist
			while(true) {
				// lass ihn eine zufällige Spalte wählen
				// nextInt schließt normalerweise den größeren Wert aus,
				// also erhöhe ihn vorher um 1
				x = ThreadLocalRandom.current().nextInt(0, spielfeld.getWidth());
				y = ((VierGewinntSpielfeld)spielfeld).placeStone(spieler, x);
				if( y >= 0 ) break;	// falls der Stein platziert wurde akzeptiere den Zug
			}
		}
		pushTurn(spieler,x,y);	// protokolliere den Zug
	}

	public void checkForEndOfGame() {
		// teste auf Sieg
		Spieler winner = spielfeld.checkForWinner();
		if( winner != null ) {
			System.out.println(winner.getName()+" hat gewonnen!");
			System.exit(0);
		}
	}

	@Override
	protected void integrateTurn(Turn turn) {
		// TODO: implement this (online)functionality
	};

	protected void stopGame() {
		System.exit(0);
	}
}
