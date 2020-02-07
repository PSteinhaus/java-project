package vierGewinntUndChomp;

import java.io.Serializable;

public class Spieler implements Serializable {
	private String name;
	private Character symbol;
	enum PlayerType {
		HUMAN, COMPUTER
	}
	private PlayerType playerType;

	Spieler(String name, Character symbol, boolean human) {
		this.name = name; this.symbol = symbol;
		if(human) playerType = PlayerType.HUMAN;
		else playerType = PlayerType.COMPUTER;
	}
	public boolean isHuman() { return playerType == PlayerType.HUMAN; }
	public void setName(String name) { this.name = name; };
	public String getName() { return name; }
	public void setSymbol(Character symbol) { this.symbol = symbol; };
	public Character getSymbol() { return symbol; }
}
