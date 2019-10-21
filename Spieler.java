public class Spieler {
	private String name;
	private Character symbol;
	enum PlayerType {
		HUMAN, COMPUTER
	}
	private PlayerType playerType;

	Spieler(String name, Character symbol) { this.name = name; this.symbol = symbol; }
	public boolean isHuman() { return playerType == PlayerType.HUMAN; }
	public void setName(String name) { this.name = name; };
	public String getName() { return name; }
	public void setSymbol(Character symbol) { this.symbol = symbol; };
	public Character getSymbol() { return symbol; }
}