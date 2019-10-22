import java.util.Scanner;

public class Futtern extends Spiel {
	private final int PLAYERNUMBER = 2;	// Anzahl der Spieler
	
	Futtern() {
		spieler = new Spieler[PLAYERNUMBER];
		System.out.println("Willkommen im Chomp-Prototypen!");
		// erfasse die Namen der Spieler & die Dimensionen des Spielbretts
		int[] dimensions = getNamesAndDimensions();
		spielfeld = new FutternSpielfeld(dimensions[0],dimensions[1]);
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
			// bitte um Eingabe des x-Wertes
			System.out.print("Wähle eine x-Koordinate: ");
			// lies die Zahl
			x = scanner.nextInt() - 1;
			// bitte um Eingabe des y-Wertes
			System.out.print("Wähle eine y-Koordinate: ");
			// lies die Zahl
			y = scanner.nextInt() - 1;
			boolean accepted = ((FutternSpielfeld)spielfeld).placeStone(spieler, x,y);
			if( accepted ) break;	// falls der Stein platziert wurde akzeptiere den Zug
			System.out.println("Wähle ein anderes Feld.");	// ansonsten weise daraufhin, dass ein anderes Feld gewählt werden muss
		}
		pushTurn(spieler,x,y);	// protokolliere den Zug
	}

	public void checkForEndOfGame() {
		// teste auf Sieg
		Spieler loser = spielfeld.checkForWinner();
		if( loser != null ) {
			System.out.println(loser.getName()+" hat verloren!");
			System.exit(0);
		}
	}
}