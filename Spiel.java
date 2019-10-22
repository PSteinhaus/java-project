import java.util.*;

public abstract class Spiel implements Protokollierbar {
	protected Spieler[] spieler;
	protected Spielfeld spielfeld;
	protected Stack<Turn> turns = new Stack<Turn>();

	public abstract void startRound();
	public abstract void takeTurn(Spieler player);
	public int[] getNamesAndDimensions() {	// erfasse am Anfang des Spieles die Namen und Symbole der Spieler, sowie die Dimensionen des Spielbretts
		Scanner scanner = new Scanner(System.in);
		// Namen
		for(int i=0; i<spieler.length; i++) {
			System.out.print("Spieler "+(i+1)+", gib deinen Namen ein: ");
			String newName = scanner.next();
			System.out.print("Wähle dein Symbol (genau ein Zeichen): ");
			Character newSymbol = scanner.next().charAt(0);
			spieler[i] = new Spieler(newName,newSymbol);
		}
		// Dimensionen
		int[] dimensions = new int[2];
		System.out.print("Breite des Spielbretts: ");
		dimensions[0] = scanner.nextInt();
		System.out.print("Höhe des Spielbretts: ");
		dimensions[1] = scanner.nextInt();
		return dimensions;
	};

	public abstract void checkForEndOfGame();

	public void pushTurn(Spieler player, int x, int y) {
		turns.push( new Turn(player,x,y) );
	};
	public void popTurn() {
		turns.pop();
	};
}