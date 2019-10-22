import java.util.Scanner;

public class MilestoneOne {
	public static void main(String[] args) {
		Spiel aktivesSpiel = null;
		// Lass einen Scanner den Kommandozeilen-input mitlesen.
		Scanner scanner = new Scanner(System.in);
		// Lass den Nutzer ein Spiel auswählen.
		int auswahl = 0;
		while( auswahl != 1 && auswahl != 2 ) {
			System.out.print("Wähle ein Spiel:\n1) Vier Gewinnt\n2) Futtern\n");
			auswahl = scanner.nextInt();
		}
		switch(auswahl) {
			case 1:
				aktivesSpiel = new VierGewinnt();
				break;
			case 2:
				aktivesSpiel = new Futtern();
				break;
		}
		// Spiele das Spiel.
		while(true)
			aktivesSpiel.startRound();
	}
}