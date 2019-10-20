public class Spieler {
	private String name;
	enum PlayerType {
		HUMAN, COMPUTER
	}
	private PlayerType playerType;

	Spieler(String name) { this.name = name; }
	public boolean isHuman() { return playerType == PlayerType.HUMAN; }
	public void setName(String name) { this.name = name; };
	public String getName() { return name; }
}