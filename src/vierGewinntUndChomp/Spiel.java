package vierGewinntUndChomp;

import chatServerUndClient.ChatServerThread;
import chatServerUndClient.GameSession;
import chatServerUndClient.Helper;

import java.io.IOException;
import java.util.*;

import static chatServerUndClient.Helper.*;

public abstract class Spiel implements Protokollierbar {
	public Spieler activePlayer = null;
	protected Spieler[] spieler;
	protected Spielfeld spielfeld;
	protected GameSession session = null;
	protected boolean isServer = false;

	public abstract void startRound();		// ein Durchgang (eine Runde)
	public abstract void takeTurn(Spieler player);
	public int[] getNamesAndDimensions() {	// erfasse am Anfang des Spieles die Namen und Symbole der Spieler (und ob sie Menschen sind), sowie die Dimensionen des Spielbretts
		Scanner scanner = new Scanner(System.in);
		// Namen
		for(int i=0; i<spieler.length; i++) {
			System.out.print("Spieler "+(i+1)+", gib deinen Namen ein: ");
			String newName = scanner.next();
			System.out.print("Wähle dein Symbol (genau ein Zeichen): ");
			Character newSymbol = scanner.next().charAt(0);
			System.out.print("Bist du ein Mensch? (j/n) ");
			boolean human = scanner.next().charAt(0) == 'j';
			spieler[i] = new Spieler(newName,newSymbol,human);
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
	public void pushTurn(Turn turn) { turns.push(turn); };
	public void popTurn() {
		turns.pop();
	};
	
	public void receiveInput(ChatServerThread player) { // receive input from a client (player) and try to follow it
		// read how many bytes
		int numberOfBytes = player.readInt();
		// get the data
		byte[] data;
		try {
			data = player.readBytes(numberOfBytes);
		} catch(IOException ioe) {
			// if the input can't be taken ignore it
			System.out.println("Error receiving player input: "+ioe.getMessage());
			return;
		}
		try {
			Turn newTurn = (Turn) Helper.deserialize(data);
			System.out.println("now integrating a turn");
			integrateTurn(newTurn);
		} catch(IOException | ClassNotFoundException ioe) {
			stopGame();
			System.out.println("Error reading received turn: "+ioe.getMessage());
		}
	}

	public void receiveUpdate(byte[] asBytes) {
		try {
			Turn receivedTurn = (Turn) Helper.deserialize(asBytes);
			if (receivedTurn != null) {
				System.out.println("now integrating a turn");
				integrateTurn(receivedTurn);
			}
			else {
				System.out.println("Stopping game because turn is null");
				stopGame();
			}
		} catch(IOException | ClassNotFoundException ioe) {
			stopGame();
			System.out.println("Error reading received turn: "+ioe.getMessage());
		}
	}

	public abstract void stopGame();

	protected abstract boolean integrateTurn(Turn turn);
	
	void sendOutput(byte[] output) {
		// send an update to all the players via the game session
		session.sendGameOutput(output);
	}
	
	public void stop() {
		
	} // stop the game

	/*
	private void sendTurn(Turn turn) {
		// send an update to the playersr ("I want to do this ...")
		try {
			session.takePlayerInput() ;sendOutput(serialize(turn));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
