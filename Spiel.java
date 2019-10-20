import java.util.*;

public abstract class Spiel implements Protokollierbar {
	protected Spieler[] spieler;
	protected Spielfeld spielfeld;
	protected Stack<Turn> turns = new Stack<Turn>();

	public abstract void startRound();
	public abstract void takeTurn(Spieler player);

	public void pushTurn(Spieler player, int x, int y) {
		turns.push( new Turn(player,x,y) );
	};
	public void popTurn() {
		turns.pop();
	};
}