public class MilestoneOne {
	public static void main(String[] args) {
		Spiel aktivesSpiel = new VierGewinnt();
		while(true)
			aktivesSpiel.startRound();
	}
}