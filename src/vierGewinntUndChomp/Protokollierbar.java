package vierGewinntUndChomp;

import java.util.*;

public interface Protokollierbar {
	// speichert Z�ge in einem Kellerspeicher(Stack)
	static final Stack<Turn> turns = new Stack<Turn>();
	public void pushTurn(Spieler player, int x, int y);
	public void popTurn();
}
