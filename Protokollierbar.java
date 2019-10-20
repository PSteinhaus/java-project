public interface Protokollierbar {
	// speichert Züge in einem Kellerspeicher(Stack)
	public void pushTurn(Spieler player, int x, int y);
	public void popTurn();
}